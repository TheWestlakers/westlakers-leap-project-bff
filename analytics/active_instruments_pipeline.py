"""
Active Instruments Aggregation Pipeline
Derives insights from the master fact table (fact_trading_volumes_hourly).
Aggregates trading data to identify most active instruments by date.

Architecture:
- Reads from: fact_trading_volumes_hourly (master fact table)
- Writes to: agg_instrument_activity
- All data is aggregated from the stable master fact table
"""
import logging
from datetime import datetime, timedelta
from decimal import Decimal
from typing import List, Dict, Any, Optional
from config import DatabaseConfig
from connection import DatabaseConnection

logger = logging.getLogger(__name__)


class ActiveInstrumentsPipeline:
    """
    Aggregation pipeline for active instruments analysis.
    
    Reads from master fact table and creates daily instrument activity aggregations.
    Calculates metrics: trades, volume, value, unique clients, activity ranking.
    
    Data flow: fact_trading_volumes_hourly → agg_instrument_activity
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
        Execute active instruments aggregation pipeline
        
        Args:
            days_back: Number of days to aggregate (default: 30)
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            logger.info("=" * 80)
            logger.info("Starting Active Instruments Pipeline")
            logger.info(f"Aggregating {days_back} days of data")
            logger.info("=" * 80)
            
            if not self.analytics_db.connect():
                logger.error("Failed to connect to analytics database")
                return False
            
            # Calculate date range
            end_date = datetime.now()
            start_date = end_date - timedelta(days=days_back)
            
            # Aggregate instrument activity
            if not self._aggregate_instrument_activity(start_date, end_date):
                logger.error("Failed to aggregate instrument activity")
                return False
            
            # Print statistics
            self._print_statistics()
            
            logger.info("=" * 80)
            logger.info("Active Instruments Pipeline completed successfully")
            logger.info("=" * 80)
            
            return True
            
        except Exception as e:
            logger.error(f"Pipeline execution failed: {e}", exc_info=True)
            self.stats['errors'] += 1
            return False
        finally:
            self.analytics_db.disconnect()
    
    def _aggregate_instrument_activity(self, start_date: datetime, end_date: datetime) -> bool:
        """
        Aggregate trading data by instrument and date
        
        Args:
            start_date: Start date for aggregation
            end_date: End date for aggregation
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Get date range from dim_date_time
            date_query = """
                SELECT DISTINCT date, EXTRACT(DAY FROM full_datetime)::INT as day_key
                FROM dim_date_time
                WHERE full_datetime >= %s AND full_datetime < %s
                ORDER BY date
            """
            
            date_records = self.analytics_db.fetch_data(date_query, (start_date, end_date))
            
            if not date_records:
                logger.warning("No dates found in the specified range")
                return False
            
            # For each date, aggregate by instrument
            insert_query = """
                INSERT INTO agg_instrument_activity 
                (date_key, instrument_key, total_trades, total_volume, total_value, 
                 unique_accounts, unique_clients, avg_trade_value, activity_rank)
                WITH daily_agg AS (
                    SELECT 
                        %s::INT as date_key,
                        ftv.instrument_key,
                        COUNT(*) as total_trades,
                        SUM(ftv.total_volume) as total_volume,
                        SUM(ftv.total_value) as total_value,
                        COUNT(DISTINCT ftv.account_key) as unique_accounts,
                        COUNT(DISTINCT ftv.client_key) as unique_clients,
                        CASE 
                            WHEN COUNT(*) > 0 THEN SUM(ftv.total_value) / COUNT(*)
                            ELSE 0
                        END as avg_trade_value
                    FROM fact_trading_volumes_hourly ftv
                    JOIN dim_date_time ddt ON ftv.datetime_key = ddt.datetime_key
                    WHERE ddt.date = %s AND ftv.instrument_key IS NOT NULL
                    GROUP BY ftv.instrument_key
                ),
                ranked AS (
                    SELECT 
                        *,
                        ROW_NUMBER() OVER (ORDER BY total_trades DESC) as activity_rank
                    FROM daily_agg
                )
                SELECT 
                    date_key,
                    instrument_key,
                    total_trades,
                    total_volume,
                    total_value,
                    unique_accounts,
                    unique_clients,
                    avg_trade_value,
                    activity_rank
                FROM ranked
                ON CONFLICT (date_key, instrument_key) 
                DO UPDATE SET
                    total_trades = EXCLUDED.total_trades,
                    total_volume = EXCLUDED.total_volume,
                    total_value = EXCLUDED.total_value,
                    unique_accounts = EXCLUDED.unique_accounts,
                    unique_clients = EXCLUDED.unique_clients,
                    avg_trade_value = EXCLUDED.avg_trade_value,
                    activity_rank = EXCLUDED.activity_rank
            """
            
            count = 0
            for date_record in date_records:
                date_val = date_record['date']
                # Get date_key from dim_date_time (using first datetime of that day)
                date_key_query = """
                    SELECT EXTRACT(DAY FROM full_datetime)::INT as day_key
                    FROM dim_date_time
                    WHERE date = %s
                    LIMIT 1
                """
                date_key_result = self.analytics_db.fetch_data(date_key_query, (date_val,))
                
                if not date_key_result:
                    logger.warning(f"Could not find date_key for {date_val}")
                    continue
                
                date_key = date_key_result[0]['day_key']
                
                if self.analytics_db.execute_query(insert_query, (date_key, date_val)):
                    count += 1
            
            self.stats['records_loaded'] = count
            logger.info(f"Loaded {count} records into agg_instrument_activity")
            return True
            
        except Exception as e:
            logger.error(f"Failed to aggregate instrument activity: {e}", exc_info=True)
            return False
    
    def _print_statistics(self):
        """Print pipeline statistics"""
        logger.info("")
        logger.info("-" * 80)
        logger.info("Active Instruments Pipeline Statistics:")
        logger.info("-" * 80)
        for key, value in self.stats.items():
            logger.info(f"  {key:.<50} {value:>10}")
        logger.info("-" * 80)
