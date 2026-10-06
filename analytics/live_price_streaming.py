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
        polling_interval: float = 3.0
    ):
        """
        Initialize the live price streamer
        
        Args:
            redis_host: Redis server hostname
            redis_port: Redis server port
            redis_db: Redis database number
            tickers: List of stock tickers to stream (default: ['AAPL', 'MSFT', 'GOOGL'])
            polling_interval: Seconds between price polls (default: 1.0)
        """
        self.redis_host = redis_host
        self.redis_port = redis_port
        self.redis_db = redis_db
        self.polling_interval = polling_interval
        self.tickers = tickers or ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'TSLA']
        self.redis_client: Optional[redis.Redis] = None
        self.running = False
        
        # Configure yfinance
        yf.config.network.retries = 3
        yf.config.debug.hide_exceptions = False
        
        logger.info(f"Initialized LivePriceStreamer with tickers: {self.tickers}")
    
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
        Poll all tickers once and publish to Redis
        
        Returns:
            int: Number of successfully fetched prices
        """
        successful = 0
        
        for ticker in self.tickers:
            price_data = self.fetch_current_price(ticker)
            if price_data:
                self.publish_to_redis(price_data)
                successful += 1
                
                # Log the update
                change = price_data.get('change', 'N/A')
                change_pct = price_data.get('changePct', 'N/A')
                current_price = price_data.get('currentPrice', 'N/A')
                logger.info(
                    f"{ticker}: ${current_price} ({change:+} {change_pct:+}%) "
                    f"[Bid: ${price_data.get('bid', 'N/A')} | Ask: ${price_data.get('ask', 'N/A')}]"
                )
        
        return successful
    
    def run(self):
        """
        Start the continuous polling loop (1 per second)
        """
        if not self.connect():
            logger.error("Failed to connect to Redis. Exiting.")
            return
        
        self.running = True
        logger.info(f"Starting live price streaming at {self.polling_interval}s intervals")
        
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
                start_time = time.time()
                
                logger.info(f"--- Poll #{iteration} ---")
                successful = self.poll_once()
                logger.info(f"Successfully fetched {successful}/{len(self.tickers)} prices")
                
                # Calculate sleep time to maintain 1-second interval
                elapsed = time.time() - start_time
                sleep_time = max(0, self.polling_interval - elapsed)
                
                if sleep_time > 0:
                    time.sleep(sleep_time)
                else:
                    logger.warning(f"Poll took {elapsed:.2f}s (longer than interval)")
        
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
    
    # Custom tickers can be passed as comma-separated string
    tickers_str = os.getenv('TICKERS', 'AAPL,MSFT,GOOGL,AMZN,TSLA')
    tickers = [t.strip() for t in tickers_str.split(',')]
    
    logger.info("=" * 70)
    logger.info("LIVE PRICE STREAMING SERVICE")
    logger.info("=" * 70)
    logger.info(f"Redis: {redis_host}:{redis_port}/{redis_db}")
    logger.info(f"Polling Interval: {polling_interval}s")
    logger.info(f"Tickers: {tickers}")
    logger.info("=" * 70)
    
    # Create and run streamer
    streamer = LivePriceStreamer(
        redis_host=redis_host,
        redis_port=redis_port,
        redis_db=redis_db,
        tickers=tickers,
        polling_interval=polling_interval
    )
    
    streamer.run()


if __name__ == '__main__':
    main()
