"""
Client Activity Trends ETL Pipeline
Aggregates trading data by client and date to identify trends and engagement patterns
Populates agg_client_activity_daily table
"""
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Any
from config import DatabaseConfig
from connection import DatabaseConnection

logger = logging.getLogger(__name__)


class ClientActivityTrendsPipeline:
    """
    ETL Pipeline for client activity trends analysis
    - Aggregates fact_trading_volumes_hourly by client and date
    - Calculates: trades, volume, value, unique instruments, trading days active
    - Identifies client engagement and activity patterns
    """
    
    def __init__(self):
        """Initialize pipeline with database connections"""
        self.analytics_db = DatabaseConnection(
            DatabaseConfig.get_analytics_db_config(),
            'analytics'
        )
        self.stats = {
            'records_aggregated': 0,
            'records_loaded': 0,
            'errors': 0
        }
    
    def run(self, days_back: int = 30) -> bool:
        """
        Execute client activity trends pipeline
        
        Args:
            days_back: Number of days to aggregate (default: 30)
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            logger.info("=" * 80)
            logger.info("Starting Client Activity Trends Pipeline")
            logger.info(f"Aggregating {days_back} days of data")
            logger.info("=" * 80)
            
            if not self.analytics_db.connect():
                logger.error("Failed to connect to analytics database")
                return False
            
            # Calculate date range
            end_date = datetime.now()
            start_date = end_date - timedelta(days=days_back)
            
            # Aggregate client activity
            if not self._aggregate_client_activity(start_date, end_date):
                logger.error("Failed to aggregate client activity")
                return False
            
            # Print statistics
            self._print_statistics()
            
            logger.info("=" * 80)
            logger.info("Client Activity Trends Pipeline completed successfully")
            logger.info("=" * 80)
            
            return True
            
        except Exception as e:
            logger.error(f"Pipeline execution failed: {e}", exc_info=True)
            self.stats['errors'] += 1
            return False
        finally:
            self.analytics_db.disconnect()
    
    def _aggregate_client_activity(self, start_date: datetime, end_date: datetime) -> bool:
        """
        Aggregate trading data by client and date
        
        Args:
            start_date: Start date for aggregation
            end_date: End date for aggregation
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Get distinct date keys and clients from fact table
            date_query = """
                SELECT DISTINCT 
                    EXTRACT(DAY FROM ddt.full_datetime)::INT as date_key,
                    ddt.date
                FROM dim_date_time ddt
                WHERE ddt.full_datetime >= %s AND ddt.full_datetime < %s
                ORDER BY ddt.date
            """
            
            date_records = self.analytics_db.fetch_data(date_query, (start_date, end_date))
            
            if not date_records:
                logger.warning("No dates found in the specified range")
                return False
            
            # For each date, aggregate by client
            insert_query = """
                INSERT INTO agg_client_activity_daily 
                (date_key, client_key, client_segment, account_count, total_trades, 
                 total_volume, total_value, unique_instruments, avg_trade_value, 
                 trading_days_active)
                WITH daily_agg AS (
                    SELECT 
                        %s::INT as date_key,
                        ftv.client_key,
                        dc.client_segment,
                        COUNT(DISTINCT ftv.account_key) as account_count,
                        COUNT(*) as total_trades,
                        SUM(ftv.total_volume) as total_volume,
                        SUM(ftv.total_value) as total_value,
                        COUNT(DISTINCT ftv.instrument_key) as unique_instruments,
                        CASE 
                            WHEN COUNT(*) > 0 THEN SUM(ftv.total_value) / COUNT(*)
                            ELSE 0
                        END as avg_trade_value,
                        1 as trading_days_active
                    FROM fact_trading_volumes_hourly ftv
                    JOIN dim_date_time ddt ON ftv.datetime_key = ddt.datetime_key
                    JOIN dim_clients dc ON ftv.client_key = dc.client_key
                    WHERE ddt.date = %s AND ftv.client_key IS NOT NULL
                    GROUP BY ftv.client_key, dc.client_segment
                )
                SELECT 
                    date_key,
                    client_key,
                    client_segment,
                    account_count,
                    total_trades,
                    total_volume,
                    total_value,
                    unique_instruments,
                    avg_trade_value,
                    trading_days_active
                FROM daily_agg
                ON CONFLICT (date_key, client_key) 
                DO UPDATE SET
                    client_segment = EXCLUDED.client_segment,
                    account_count = EXCLUDED.account_count,
                    total_trades = EXCLUDED.total_trades,
                    total_volume = EXCLUDED.total_volume,
                    total_value = EXCLUDED.total_value,
                    unique_instruments = EXCLUDED.unique_instruments,
                    avg_trade_value = EXCLUDED.avg_trade_value,
                    trading_days_active = EXCLUDED.trading_days_active
            """
            
            count = 0
            for date_record in date_records:
                date_key = date_record['date_key']
                date_val = date_record['date']
                
                if self.analytics_db.execute_query(insert_query, (date_key, date_val)):
                    count += 1
            
            self.stats['records_loaded'] = count
            logger.info(f"Loaded {count} records into agg_client_activity_daily")
            return True
            
        except Exception as e:
            logger.error(f"Failed to aggregate client activity: {e}", exc_info=True)
            return False
    
    def _print_statistics(self):
        """Print pipeline statistics"""
        logger.info("")
        logger.info("-" * 80)
        logger.info("Client Activity Trends Pipeline Statistics:")
        logger.info("-" * 80)
        for key, value in self.stats.items():
            logger.info(f"  {key:.<50} {value:>10}")
        logger.info("-" * 80)
