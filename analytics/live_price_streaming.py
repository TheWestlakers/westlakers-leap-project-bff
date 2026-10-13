"""
Real-Time Live Price Streaming Service
Continuously polls yfinance API at 1 per second and streams prices to Redis.

Features:
- Fetches live prices from yfinance without rate limits
- Stores prices in Redis streams for real-time consumption
- Maintains latest price cache for quick lookups
- Supports multiple tickers
- Includes error handling and automatic reconnection

Redis Data Structure:
- Stream: prices:stream - All price updates with timestamp
- Hash: prices:latest - Latest price for each ticker
- Hash: prices:metadata - Last update time and stats
"""

import redis
import yfinance as yf
import asyncio
import time
import json
import logging
from logging.handlers import RotatingFileHandler
from datetime import datetime
from typing import List, Dict, Any, Optional, Set
from pathlib import Path
import signal
import sys
import threading
from queue import Queue

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        RotatingFileHandler(
            'live_price_streaming.log',
            maxBytes=256 * 1024,
            backupCount=1
        ),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

ACTIVE_TICKERS_SET = 'prices:tickers:active'
WATCHLIST_TICKERS_SET = 'prices:tickers:watchlist'
FOCUS_TICKERS_SET = 'prices:tickers:focus'
FOCUS_LEASE_KEY_PREFIX = 'prices:focus:lease:'


class LivePriceStreamer:
    """Streams live prices from yfinance to Redis in real-time"""
    
    def __init__(
        self,
        redis_host: str = 'localhost',
        redis_port: int = 6379,
        redis_db: int = 0,
        tickers: Optional[List[str]] = None,
        polling_interval: float = 1.0,
        stagger_interval: float = 1.0,
        focus_polling_interval: float = 1.0
    ):
        """
        Initialize the live price streamer
        
        Args:
            redis_host: Redis server hostname
            redis_port: Redis server port
            redis_db: Redis database number
            tickers: List of stock tickers to stream (default: ['AAPL', 'MSFT', 'GOOGL'])
            polling_interval: Seconds per cycle (default: 3.0)
            stagger_interval: Seconds between sequential fetch starts (default: 1.0)
        """
        self.redis_host = redis_host
        self.redis_port = redis_port
        self.redis_db = redis_db
        self.polling_interval = polling_interval
        self.stagger_interval = stagger_interval
        self.focus_polling_interval = focus_polling_interval
        self.tickers = tickers or ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'TSLA']
        self.watchlist_tickers: Set[str] = set(self.tickers)
        self.focused_tickers: Set[str] = set()
        self.redis_client: Optional[redis.Redis] = None
        self.running = False
        self.live_connection = None
        
        # Staggered fetching setup
        self.ticker_slots: Dict[float, List[str]] = {}  # time_offset -> [tickers]
        self._assign_tickers_to_slots()
        
        # Configure yfinance
        yf.config.network.retries = 3
        yf.config.debug.hide_exceptions = False
        
        logger.info(f"Initialized LivePriceStreamer with tickers: {self.tickers}")
        logger.info(f"Polling interval: {self.polling_interval}s, Stagger interval: {self.stagger_interval}s")
        logger.info(f"Ticker slot distribution: {self.ticker_slots}")
    
    def connect(self) -> bool:
        """
        Connect to Redis
        
        Returns:
            bool: True if connection successful
        """
        try:
            self.redis_client = redis.Redis(
                host=self.redis_host,
                port=self.redis_port,
                db=self.redis_db,
                decode_responses=True,
                socket_keepalive=True
            )
            # Test connection
            self.redis_client.ping()
            logger.info(f"Connected to Redis at {self.redis_host}:{self.redis_port}")
            return True
        except redis.ConnectionError as e:
            logger.error(f"Failed to connect to Redis: {e}")
            return False
    
    def disconnect(self):
        """Close Redis connection"""
        if self.redis_client:
            self.redis_client.close()
            logger.info("Disconnected from Redis")
    
    def _assign_tickers_to_slots(self):
        """
        Assign tickers to time slots within the polling cycle.
        
        For a 3-second cycle with 1-second stagger intervals:
        - Slot 0.0s: tickers[0], tickers[3], tickers[6], ...
        - Slot 1.0s: tickers[1], tickers[4], tickers[7], ...
        - Slot 2.0s: tickers[2], tickers[5], tickers[8], ...
        
        This ensures all tickers are fetched once per cycle with overlapping calls.
        """
        self.ticker_slots.clear()
        num_slots = max(1, int(self.polling_interval / self.stagger_interval))
        
        for i, ticker in enumerate(self.tickers):
            slot_offset = (i % num_slots) * self.stagger_interval
            if slot_offset not in self.ticker_slots:
                self.ticker_slots[slot_offset] = []
            self.ticker_slots[slot_offset].append(ticker)
        
        logger.info(f"Assigned {len(self.tickers)} tickers to {num_slots} time slots")

    def refresh_tickers_from_redis(self):
        """Reload watchlist and focused ticker symbols from Redis."""
        if not self.redis_client:
            return

        try:
            watchlist = self.redis_client.smembers(WATCHLIST_TICKERS_SET)
            focused = self._get_active_focus_tickers()

            normalize = lambda values: {
                ticker.strip().upper() for ticker in values if ticker and ticker.strip()
            }
            normalized_watchlist = normalize(watchlist)
            normalized_focused = normalize(focused)
            normalized_watchlist -= normalized_focused

            if normalized_watchlist != self.watchlist_tickers:
                self.watchlist_tickers = normalized_watchlist
                self.tickers = sorted(normalized_watchlist)
                self._assign_tickers_to_slots()
                logger.info(f"Updated watchlist tickers from Redis: {self.tickers}")
            if normalized_focused != self.focused_tickers:
                self.focused_tickers = normalized_focused
                logger.info(f"Updated focused tickers from Redis: {sorted(self.focused_tickers)}")
        except Exception as e:
            logger.warning(f"Failed refreshing tickers from Redis: {e}")

    def _get_active_focus_tickers(self) -> Set[str]:
        """Read hot symbols from five-minute focus leases, ignoring expired set members."""
        if not self.redis_client:
            return set()

        prefix_length = len(FOCUS_LEASE_KEY_PREFIX)
        focused = set()
        for key in self.redis_client.scan_iter(match=f'{FOCUS_LEASE_KEY_PREFIX}*'):
            key_text = key.decode() if isinstance(key, bytes) else key
            suffix = key_text[prefix_length:]
            ticker = suffix.split(':', 1)[0]
            if ticker:
                focused.add(ticker.upper())
        return focused

    @staticmethod
    def _history_value(history, field: str, ticker: str):
        """Read a value from either yfinance history column layout."""
        try:
            if hasattr(history.columns, 'levels'):
                for key in ((field, ticker), (ticker, field)):
                    if key in history.columns:
                        return history[key].dropna().iloc[-1]
            if field in history.columns:
                return history[field].dropna().iloc[-1]
        except (IndexError, KeyError):
            pass
        return None

    def fetch_watchlist_batch(self) -> Dict[str, Dict[str, Any]]:
        """Fetch all watchlist prices with one yfinance chart request."""
        if not self.watchlist_tickers:
            return {}
        try:
            history = yf.Tickers(' '.join(sorted(self.watchlist_tickers))).history(
                period='1d', interval='1m', prepost=True, actions=False,
                auto_adjust=False, threads=True, group_by='column',
                progress=False, timeout=10
            )
            results = {}
            for ticker in sorted(self.watchlist_tickers):
                current_price = self._history_value(history, 'Close', ticker)
                if current_price is not None:
                    volume = self._history_value(history, 'Volume', ticker)
                    results[ticker] = {
                        'ticker': ticker,
                        'timestamp': datetime.now().isoformat(),
                        'currentPrice': float(current_price),
                        'volume': float(volume) if volume is not None else None
                    }
            return results
        except Exception as e:
            logger.warning(f"Error fetching watchlist batch: {e}")
            return {}

    def _handle_live_message(self, message: Dict[str, Any]):
        """Normalize a yfinance websocket message and publish it."""
        ticker = message.get('id') or message.get('symbol')
        current_price = message.get('price') or message.get('lastPrice')
        if not ticker or current_price is None:
            return
        previous_close = message.get('previous_close') or message.get('previousClose')
        price_data = {
            'ticker': ticker.upper(), 'timestamp': datetime.now().isoformat(),
            'currentPrice': current_price, 'previousClose': previous_close,
            'volume': message.get('day_volume') or message.get('volume')
        }
        if previous_close:
            price_data['change'] = round(current_price - previous_close, 2)
            price_data['changePct'] = round((current_price - previous_close) / previous_close * 100, 2)
        self.publish_to_redis(price_data)

    def _run_live_connection(self):
        """Run one async websocket and update its subscriptions in place."""
        try:
            asyncio.run(self._run_async_live_connection())
        except Exception as e:
            logger.warning(f"Focused live connection stopped: {e}")

    async def _run_async_live_connection(self):
        """Subscribe and unsubscribe focused symbols without reconnecting."""
        websocket = yf.AsyncWebSocket(verbose=False)
        self.live_connection = websocket
        listener_task = None
        subscribed: Set[str] = set()

        try:
            while self.running:
                desired = set(self.focused_tickers)
                added = sorted(desired - subscribed)
                removed = sorted(subscribed - desired)

                if added:
                    await websocket.subscribe(added)
                    subscribed.update(added)
                if removed:
                    await websocket.unsubscribe(removed)
                    subscribed.difference_update(removed)

                if subscribed and listener_task is None:
                    listener_task = asyncio.create_task(
                        websocket.listen(message_handler=self._handle_live_message)
                    )
                if listener_task and listener_task.done():
                    listener_task.result()
                    listener_task = None

                await asyncio.sleep(self.focus_polling_interval)
        finally:
            if listener_task:
                listener_task.cancel()
                await asyncio.gather(listener_task, return_exceptions=True)
            await websocket.close()
            self.live_connection = None
    
    def _fetch_ticker_group(
        self,
        tickers: List[str],
        results: Dict[str, Optional[Dict[str, Any]]]
    ):
        """
        Fetch prices for a group of tickers and store in results dict.
        This is designed to run in a separate thread.
        
        Args:
            tickers: List of tickers to fetch
            results: Shared dictionary to store results
        """
        for ticker in tickers:
            price_data = self.fetch_current_price(ticker)
            if price_data:
                results[ticker] = price_data
    
    def fetch_current_price(self, ticker: str) -> Optional[Dict[str, Any]]:
        """
        Fetch current price and info for a ticker
        
        Args:
            ticker: Stock ticker symbol
            
        Returns:
            Dict with price data or None if error
        """
        try:
            tick = yf.Ticker(ticker)
            info = tick.info
            
            current_price = info.get('currentPrice')
            previous_close = info.get('previousClose')
            
            price_data = {
                'ticker': ticker,
                'timestamp': datetime.now().isoformat(),
                'currentPrice': current_price,
                'previousClose': previous_close,
                'volume': info.get('volume'),
                'marketCap': info.get('marketCap'),
                'peRatio': info.get('trailingPE'),
                'fiftyTwoWeekHigh': info.get('fiftyTwoWeekHigh'),
                'fiftyTwoWeekLow': info.get('fiftyTwoWeekLow')
            }
            
            # Calculate change if we have price data
            if current_price and previous_close:
                change = current_price - previous_close
                change_pct = (change / previous_close) * 100
                price_data['change'] = round(change, 2)
                price_data['changePct'] = round(change_pct, 2)
            
            return price_data
            
        except Exception as e:
            logger.warning(f"Error fetching price for {ticker}: {e}")
            return None
    
    def publish_to_redis(self, price_data: Dict[str, Any]):
        """
        Publish price data to Redis
        
        Args:
            price_data: Price data dictionary
        """
        if not self.redis_client:
            return
        
        try:
            ticker = price_data['ticker']
            
            # Add to Redis stream (for streaming/historical data)
            stream_key = 'prices:stream'
            self.redis_client.xadd(stream_key, {
                'ticker': ticker,
                'price': str(price_data.get('currentPrice', 'N/A')),
                'change': str(price_data.get('change', 'N/A')),
                'changePct': str(price_data.get('changePct', 'N/A')),
                'timestamp': price_data['timestamp']
            })
            
            # Update latest price cache (for quick lookups)
            latest_key = f'prices:latest:{ticker}'
            self.redis_client.set(
                latest_key,
                json.dumps(price_data),
                ex=3600  # Expire after 1 hour
            )
            
            # Update metadata
            self.redis_client.hset(
                'prices:metadata',
                mapping={
                    f'{ticker}:last_update': datetime.now().isoformat(),
                    f'{ticker}:last_price': str(price_data.get('currentPrice', 'N/A'))
                }
            )
            
            # Also publish via pub/sub for real-time subscribers
            self.redis_client.publish(f'prices:{ticker}', json.dumps(price_data))
            
            logger.debug(f"Published price for {ticker}: ${price_data.get('currentPrice', 'N/A')}")
            
        except Exception as e:
            logger.error(f"Error publishing to Redis: {e}")
    
    def poll_once(self):
        """
        Fetch the watchlist in one batched request.
        
        Returns:
            int: Number of successfully fetched prices
        """
        successful = 0
        for ticker, price_data in self.fetch_watchlist_batch().items():
            if price_data:
                self.publish_to_redis(price_data)
                successful += 1
                
                change = price_data.get('change', 'N/A')
                change_pct = price_data.get('changePct', 'N/A')
                current_price = price_data.get('currentPrice', 'N/A')
                logger.info(
                    f"{ticker}: ${current_price} ({change} {change_pct}%)"
                )
        
        return successful
    
    def _delayed_fetch(
        self,
        delay: float,
        tickers: List[str],
        results: Dict[str, Optional[Dict[str, Any]]]
    ):
        """
        Fetch tickers after a delay.
        
        Args:
            delay: Seconds to wait before fetching
            tickers: List of tickers to fetch
            results: Shared dictionary to store results
        """
        if delay > 0:
            time.sleep(delay)
        
        self._fetch_ticker_group(tickers, results)
    
    def run(self):
        """
        Start the continuous polling loop with staggered fetch intervals.
        
        Each cycle:
        - Starts at time 0
        - Fetches start at 0s, 1s, 2s (staggered by stagger_interval)
        - All fetches complete within the cycle duration
        - Next cycle starts after polling_interval seconds
        """
        if not self.connect():
            logger.error("Failed to connect to Redis. Exiting.")
            return
        
        self.running = True
        self.live_thread = threading.Thread(target=self._run_live_connection, daemon=True)
        self.live_thread.start()
        logger.info(
            f"Starting live price streaming with staggered fetches\n"
            f"  Cycle interval: {self.polling_interval}s\n"
            f"  Stagger interval: {self.stagger_interval}s\n"
            f"  Max concurrent API calls: {len(self.ticker_slots)}"
        )
        
        # Handle graceful shutdown
        def signal_handler(sig, frame):
            logger.info("\nShutting down gracefully...")
            self.running = False
        
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
        
        try:
            iteration = 0
            while self.running:
                iteration += 1
                cycle_start = time.time()

                self.refresh_tickers_from_redis()
                
                logger.info(f"--- Cycle #{iteration} [Time: {datetime.now().strftime('%H:%M:%S')}] ---")
                successful = self.poll_once()
                logger.info(f"Successfully fetched {successful}/{len(self.tickers)} prices")
                
                # Calculate time until next cycle
                cycle_elapsed = time.time() - cycle_start
                sleep_time = max(0, self.polling_interval - cycle_elapsed)
                
                if sleep_time > 0:
                    logger.debug(f"Cycle took {cycle_elapsed:.2f}s, sleeping {sleep_time:.2f}s until next cycle")
                    time.sleep(sleep_time)
                else:
                    logger.warning(f"Cycle took {cycle_elapsed:.2f}s (exceeded {self.polling_interval}s interval by {cycle_elapsed - self.polling_interval:.2f}s)")
        
        except Exception as e:
            logger.error(f"Error in polling loop: {e}")
        
        finally:
            self.running = False
            if self.live_thread and self.live_thread.is_alive():
                self.live_thread.join(timeout=self.focus_polling_interval + 1)
            self.disconnect()
            logger.info("Live price streaming stopped")


def main():
    """Main entry point"""
    import os
    from dotenv import load_dotenv
    
    # Load environment variables
    root_env_path = Path(__file__).parent.parent / '.env'
    load_dotenv(dotenv_path=root_env_path)
    
    # Get configuration from environment
    redis_host = os.getenv('REDIS_HOST', 'localhost')
    redis_port = int(os.getenv('REDIS_PORT', 6379))
    redis_db = int(os.getenv('REDIS_DB', 0))
    polling_interval = float(os.getenv('POLLING_INTERVAL', 3.0))
    stagger_interval = float(os.getenv('STAGGER_INTERVAL', 1.0))
    
    # Custom tickers can be passed as comma-separated string
    tickers_str = os.getenv('TICKERS','AAPL,MSFT,GOOGL,AMZN,TSLA,MU')
    tickers = [t.strip() for t in tickers_str.split(',')]
    
    logger.info("=" * 70)
    logger.info("LIVE PRICE STREAMING SERVICE")
    logger.info("=" * 70)
    logger.info(f"Redis: {redis_host}:{redis_port}/{redis_db}")
    logger.info(f"Polling Cycle Interval: {polling_interval}s")
    logger.info(f"Stagger Interval Between Fetches: {stagger_interval}s")
    logger.info(f"Number of Tickers: {len(tickers)}")
    logger.info(f"Tickers: {tickers}")
    logger.info("=" * 70)
    
    # Create and run streamer
    streamer = LivePriceStreamer(
        redis_host=redis_host,
        redis_port=redis_port,
        redis_db=redis_db,
        tickers=tickers,
        polling_interval=polling_interval,
        stagger_interval=stagger_interval
    )
    
    streamer.run()


if __name__ == '__main__':
    main()
