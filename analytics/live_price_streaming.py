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
import time
import json
import logging
from datetime import datetime
from typing import List, Dict, Any, Optional
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
        logging.FileHandler('live_price_streaming.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


class LivePriceStreamer:
    """Streams live prices from yfinance to Redis in real-time"""
    
    def __init__(
        self,
        redis_host: str = 'localhost',
        redis_port: int = 6379,
        redis_db: int = 0,
        tickers: Optional[List[str]] = None,
        polling_interval: float = 3.0,
        stagger_interval: float = 1.0
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
        self.tickers = tickers or ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'TSLA']
        self.redis_client: Optional[redis.Redis] = None
        self.running = False
        
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
        num_slots = int(self.polling_interval / self.stagger_interval)
        
        for i, ticker in enumerate(self.tickers):
            slot_offset = (i % num_slots) * self.stagger_interval
            if slot_offset not in self.ticker_slots:
                self.ticker_slots[slot_offset] = []
            self.ticker_slots[slot_offset].append(ticker)
        
        logger.info(f"Assigned {len(self.tickers)} tickers to {num_slots} time slots")
    
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
                'bid': info.get('bid'),
                'ask': info.get('ask'),
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
        Poll all tickers with staggered starts within the cycle.
        Tickers are fetched at different time offsets to allow overlapping API calls.
        
        Returns:
            int: Number of successfully fetched prices
        """
        cycle_start = time.time()
        results: Dict[str, Optional[Dict[str, Any]]] = {}
        threads: List[threading.Thread] = []
        
        # Launch fetch threads at staggered intervals
        for slot_offset in sorted(self.ticker_slots.keys()):
            tickers_for_slot = self.ticker_slots[slot_offset]
            
            # Calculate when this slot should start
            slot_start_time = cycle_start + slot_offset
            delay = max(0, slot_start_time - time.time())
            
            # Create thread to fetch this slot's tickers
            thread = threading.Thread(
                target=self._delayed_fetch,
                args=(delay, tickers_for_slot, results),
                daemon=True
            )
            threads.append(thread)
            thread.start()
        
        # Wait for all fetch threads to complete
        for thread in threads:
            thread.join()
        
        # Publish all fetched prices to Redis
        successful = 0
        for ticker, price_data in results.items():
            if price_data:
                self.publish_to_redis(price_data)
                successful += 1
                
                change = price_data.get('change', 'N/A')
                change_pct = price_data.get('changePct', 'N/A')
                current_price = price_data.get('currentPrice', 'N/A')
                logger.info(
                    f"{ticker}: ${current_price} ({change:+} {change_pct:+}%) "
                    f"[Bid: ${price_data.get('bid', 'N/A')} | Ask: ${price_data.get('ask', 'N/A')}]"
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
