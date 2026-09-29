"""
Minimal Trading Volumes ETL Pipeline
Implements master data table architecture for data warehouse.

Pipeline Flow:
1. Extract orders from source database
2. Load stg_trading_volumes (master staging table with denormalized data)
3. Load dimension tables (date, clients, instruments, accounts)
4. Load fact_trading_volumes_hourly (aggregated from staging table)

Architecture:
- stg_trading_volumes: Denormalized master table (single source of truth for trading data)
- fact_trading_volumes_hourly: Aggregated fact table at hourly granularity
- All aggregation pipelines read from fact_trading_volumes_hourly
- This separation allows fact table reprocessing without re-extracting from source

Benefits:
- Single source of truth for raw data (stg_trading_volumes)
- Fact table can be recalculated from staging if aggregation logic changes
- All downstream aggregations depend on stable master table
- Aligns with standard data warehouse best practices (similar to Airflow architecture)
"""
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Any, Optional, Tuple
from decimal import Decimal
from config import DatabaseConfig, ETLConfig
from connection import DatabaseConnection

logger = logging.getLogger(__name__)


class TradingVolumesETL:
    """
    Master Data Architecture ETL Pipeline for trading volumes analysis
    
    Implements a master staging table pattern:
    - Extracts order data from source database
    - Loads stg_trading_volumes (denormalized master table)
    - Loads dimension tables (date, clients, instruments, accounts)
    - Loads fact_trading_volumes_hourly (aggregated from staging via SQL)
    
    All downstream aggregation pipelines read from fact_trading_volumes_hourly,
    providing a single source of truth for trading data analysis.
    """
    
    def __init__(self):
        """Initialize ETL pipeline with database connections"""
        self.source_db = DatabaseConnection(
            DatabaseConfig.get_source_db_config(), 
            'source'
        )
        self.analytics_db = DatabaseConnection(
            DatabaseConfig.get_analytics_db_config(), 
            'analytics'
        )
        self.stats = {
            'orders_extracted': 0,
            'staging_volumes_loaded': 0,
            'dim_dates_loaded': 0,
            'dim_clients_loaded': 0,
            'dim_instruments_loaded': 0,
            'dim_accounts_loaded': 0,
            'fact_volumes_loaded': 0,
            'errors': 0
        }
    
    def run(self, days_back: int = 30) -> bool:
        """
        Execute complete ETL pipeline
        
        Flow: Extract → Stage → Load Dimensions → Load Fact Table → Load Aggregations
        
        Args:
            days_back: Number of days to look back for orders (default: 30)
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            logger.info("=" * 80)
            logger.info("Starting Trading Volumes ETL Pipeline (Master Data Approach)")
            logger.info(f"Looking back {days_back} days")
            logger.info("=" * 80)
            
            # Connect to databases
            if not self._connect_databases():
                logger.error("Failed to connect to databases")
                return False
            
            # Calculate date range
            end_date = datetime.now()
            start_date = end_date - timedelta(days=days_back)
            
            # Extract orders from source database
            orders = self._extract_orders(start_date, end_date)
            if not orders:
                logger.warning("No orders found in the specified date range")
                return False
            
            logger.info(f"Extracted {len(orders)} orders")
            self.stats['orders_extracted'] = len(orders)
            
            # Load staging table (master denormalized data source)
            if not self._load_stg_trading_volumes(orders):
                logger.error("Failed to load staging table")
                return False
            
            # Load dimension tables
            if not self._load_dim_date_time(orders):
                logger.error("Failed to load dimension tables")
                return False
            
            if not self._load_dim_clients():
                logger.error("Failed to load dim_clients")
                return False
            
            if not self._load_dim_instruments():
                logger.error("Failed to load dim_instruments")
                return False
            
            if not self._load_dim_accounts():
                logger.error("Failed to load dim_accounts")
                return False
            
            # Load fact table from staging table
            if not self._load_fact_trading_volumes():
                logger.error("Failed to load fact_trading_volumes_hourly")
                return False
            
            # Print statistics
            self._print_statistics()
            
            logger.info("=" * 80)
            logger.info("Trading Volumes ETL Pipeline completed successfully")
            logger.info("=" * 80)
            
            return True
            
        except Exception as e:
            logger.error(f"Pipeline execution failed: {e}", exc_info=True)
            self.stats['errors'] += 1
            return False
        finally:
            self._disconnect_databases()
    
    def _connect_databases(self) -> bool:
        """Connect to both source and analytics databases"""
        source_ok = self.source_db.connect()
        analytics_ok = self.analytics_db.connect()
        
        if source_ok and analytics_ok:
            return True
        
        if not source_ok:
            logger.error("Failed to connect to source database")
        if not analytics_ok:
            logger.error("Failed to connect to analytics database")
        
        return False
    
    def _disconnect_databases(self):
        """Disconnect from both databases"""
        self.source_db.disconnect()
        self.analytics_db.disconnect()
    
    def _extract_orders(self, start_date: datetime, end_date: datetime) -> List[Dict[str, Any]]:
        """
        Extract orders from source database
        
        Args:
            start_date: Start date for order extraction
            end_date: End date for order extraction
            
        Returns:
            List of order records with related data
        """
        query = """
            SELECT 
                o.order_id,
                o.account_id,
                o.instrument_id,
                o.side,
                o.quantity,
                o.limit_price,
                o.placed_at,
                o.executed_at,
                u.user_id,
                u.first_name,
                u.last_name,
                i.ticker as instrument_code,
                i.name as instrument_name,
                ac.account_type_id,
                at.type_name as account_type
            FROM orders o
            JOIN accounts ac ON o.account_id = ac.account_id
            JOIN users u ON ac.user_id = u.user_id
            JOIN instruments i ON o.instrument_id = i.instrument_id
            JOIN account_types at ON ac.account_type_id = at.account_type_id
            WHERE o.executed_at >= %s AND o.executed_at <= %s
            ORDER BY o.executed_at
        """
        
        try:
            orders = self.source_db.fetch_data(query, (start_date, end_date))
            logger.info(f"Extracted {len(orders)} orders from source database")
            return orders
        except Exception as e:
            logger.error(f"Failed to extract orders: {e}")
            return []
    
    def _load_stg_trading_volumes(self, orders: List[Dict[str, Any]]) -> bool:
        """
        Load staging table (master denormalized trading data)
        
        This is the single source of truth for all downstream aggregations.
        Contains all individual trades with denormalized dimensions.
        
        Args:
            orders: List of order records from source database
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            insert_query = """
                INSERT INTO stg_trading_volumes 
                (order_id, account_id, instrument_id, user_id, side, quantity, limit_price, 
                 executed_at, account_type, first_name, last_name, instrument_code, 
                 instrument_name, instrument_type, client_segment)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (order_id) DO NOTHING
            """
            
            count = 0
            for order in orders:
                params = (
                    order['order_id'],
                    order['account_id'],
                    order['instrument_id'],
                    order['user_id'],
                    order['side'],
                    float(order['quantity']) if order['quantity'] else 0,
                    float(order['limit_price']) if order['limit_price'] else 0,
                    order['executed_at'],
                    order['account_type'],
                    order['first_name'],
                    order['last_name'],
                    order['instrument_code'],
                    order['instrument_name'],
                    'EQUITY',  # Default type - can be enhanced with real data
                    'STANDARD'  # Default segment - can be enhanced with real data
                )
                
                if self.analytics_db.execute_query(insert_query, params):
                    count += 1
            
            self.stats['staging_volumes_loaded'] = count
            logger.info(f"Loaded {count} records into stg_trading_volumes (staging table)")
            return True
            
        except Exception as e:
            logger.error(f"Failed to load stg_trading_volumes: {e}")
            return False
    
    def _load_dim_date_time(self, orders: List[Dict[str, Any]]) -> bool:
        """
        Load dimension_date_time with dates from orders
        
        Args:
            orders: List of order records
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Get unique dates from orders
            dates_set = set()
            for order in orders:
                executed_at = order['executed_at']
                if isinstance(executed_at, str):
                    executed_at = datetime.fromisoformat(executed_at)
                dates_set.add(executed_at.replace(hour=0, minute=0, second=0, microsecond=0))
            
            if not dates_set:
                logger.warning("No dates found in orders")
                return False
            
            # Generate hourly records for each date
            hourly_records = []
            for base_date in sorted(dates_set):
                for hour in range(24):
                    dt = base_date.replace(hour=hour)
                    hourly_records.append({
                        'full_datetime': dt,
                        'date': dt.date(),
                        'year': dt.year,
                        'quarter': (dt.month - 1) // 3 + 1,
                        'month': dt.month,
                        'day': dt.day,
                        'hour': dt.hour,
                        'day_of_week': dt.strftime('%A'),
                        'week_of_year': dt.isocalendar()[1],
                        'is_trading_day': dt.weekday() < 5  # Monday-Friday
                    })
            
            # Load into dimension table
            insert_query = """
                INSERT INTO dim_date_time 
                (full_datetime, date, year, quarter, month, day, hour, day_of_week, week_of_year, is_trading_day)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (full_datetime) DO NOTHING
            """
            
            count = 0
            for record in hourly_records:
                params = (
                    record['full_datetime'],
                    record['date'],
                    record['year'],
                    record['quarter'],
                    record['month'],
                    record['day'],
                    record['hour'],
                    record['day_of_week'],
                    record['week_of_year'],
                    record['is_trading_day']
                )
                
                if self.analytics_db.execute_query(insert_query, params):
                    count += 1
            
            self.stats['dim_dates_loaded'] = count
            logger.info(f"Loaded {count} records into dim_date_time")
            return True
            
        except Exception as e:
            logger.error(f"Failed to load dim_date_time: {e}")
            return False
    
    def _load_dim_clients(self) -> bool:
        """Load dim_clients from source users"""
        try:
            # Extract unique users/clients
            query = """
                SELECT DISTINCT
                    u.user_id,
                    u.first_name,
                    u.last_name,
                    'STANDARD' as client_segment,
                    u.create_date
                FROM users u
                WHERE u.status_id = 1
                ORDER BY u.user_id
            """
            
            users = self.source_db.fetch_data(query)
            
            insert_query = """
                INSERT INTO dim_clients 
                (user_id, first_name, last_name, client_segment, created_date)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT (user_id) DO NOTHING
            """
            
            count = 0
            for user in users:
                params = (
                    user['user_id'],
                    user['first_name'],
                    user['last_name'],
                    user['client_segment'],
                    user['create_date']
                )
                
                if self.analytics_db.execute_query(insert_query, params):
                    count += 1
            
            self.stats['dim_clients_loaded'] = count
            logger.info(f"Loaded {count} records into dim_clients")
            return True
            
        except Exception as e:
            logger.error(f"Failed to load dim_clients: {e}")
            return False
    
    def _load_dim_instruments(self) -> bool:
        """Load dim_instruments from source instruments"""
        try:
            query = """
                SELECT
                    i.instrument_id,
                    i.ticker,
                    i.name,
                    ac.class_name as instrument_type
                FROM instruments i
                JOIN asset_classes ac ON i.asset_class_id = ac.asset_class_id
                ORDER BY i.instrument_id
            """
            
            instruments = self.source_db.fetch_data(query)
            
            insert_query = """
                INSERT INTO dim_instruments 
                (instrument_id, instrument_code, instrument_name, instrument_type)
                VALUES (%s, %s, %s, %s)
                ON CONFLICT (instrument_id) DO NOTHING
            """
            
            count = 0
            for inst in instruments:
                params = (
                    inst['instrument_id'],
                    inst['ticker'],
                    inst['name'],
                    inst['instrument_type']
                )
                
                if self.analytics_db.execute_query(insert_query, params):
                    count += 1
            
            self.stats['dim_instruments_loaded'] = count
            logger.info(f"Loaded {count} records into dim_instruments")
            return True
            
        except Exception as e:
            logger.error(f"Failed to load dim_instruments: {e}")
            return False
    
    def _load_dim_accounts(self) -> bool:
        """Load dim_accounts from source accounts"""
        try:
            query = """
                SELECT
                    a.account_id,
                    a.user_id,
                    at.type_name as account_type,
                    'USD' as currency,
                    a.created_at
                FROM accounts a
                JOIN account_types at ON a.account_type_id = at.account_type_id
                WHERE a.account_status_id = 1
                ORDER BY a.account_id
            """
            
            accounts = self.source_db.fetch_data(query)
            
            insert_query = """
                INSERT INTO dim_accounts 
                (account_id, client_key, account_type, currency, created_date)
                VALUES (
                    %s,
                    (SELECT client_key FROM dim_clients WHERE user_id = %s),
                    %s,
                    %s,
                    %s
                )
                ON CONFLICT (account_id) DO NOTHING
            """
            
            count = 0
            for acc in accounts:
                params = (
                    acc['account_id'],
                    acc['user_id'],
                    acc['account_type'],
                    acc['currency'],
                    acc['created_at']
                )
                
                if self.analytics_db.execute_query(insert_query, params):
                    count += 1
            
            self.stats['dim_accounts_loaded'] = count
            logger.info(f"Loaded {count} records into dim_accounts")
            return True
            
        except Exception as e:
            logger.error(f"Failed to load dim_accounts: {e}")
            return False
    
    def _load_fact_trading_volumes(self) -> bool:
        """
        Load fact_trading_volumes_hourly by aggregating staging table
        
        Reads from stg_trading_volumes (master table) and creates aggregated fact table.
        This separation allows fact table to be reprocessed if logic changes.
        
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # SQL to aggregate staging table into fact table
            # Groups by hour, instrument, and account
            agg_query = """
                INSERT INTO fact_trading_volumes_hourly 
                (datetime_key, instrument_key, account_key, client_key, trade_count, 
                 total_volume, total_value, avg_price, min_price, max_price)
                SELECT 
                    ddt.datetime_key,
                    di.instrument_key,
                    da.account_key,
                    dc.client_key,
                    COUNT(*) as trade_count,
                    SUM(stv.quantity) as total_volume,
                    SUM(stv.quantity * stv.limit_price) as total_value,
                    AVG(CASE WHEN stv.limit_price > 0 THEN stv.limit_price ELSE NULL END) as avg_price,
                    MIN(CASE WHEN stv.limit_price > 0 THEN stv.limit_price ELSE NULL END) as min_price,
                    MAX(CASE WHEN stv.limit_price > 0 THEN stv.limit_price ELSE NULL END) as max_price
                FROM stg_trading_volumes stv
                JOIN dim_date_time ddt ON DATE_TRUNC('hour', stv.executed_at) = ddt.full_datetime
                JOIN dim_instruments di ON stv.instrument_id = di.instrument_id
                JOIN dim_accounts da ON stv.account_id = da.account_id
                JOIN dim_clients dc ON stv.user_id = dc.user_id
                WHERE stv.executed_at IS NOT NULL
                GROUP BY ddt.datetime_key, di.instrument_key, da.account_key, dc.client_key
                ON CONFLICT (datetime_key, instrument_key, account_key) 
                DO UPDATE SET
                    trade_count = EXCLUDED.trade_count,
                    total_volume = EXCLUDED.total_volume,
                    total_value = EXCLUDED.total_value,
                    avg_price = EXCLUDED.avg_price,
                    min_price = EXCLUDED.min_price,
                    max_price = EXCLUDED.max_price
            """
            
            # Execute aggregation
            if self.analytics_db.execute_query(agg_query):
                # Get count of inserted records
                count_query = "SELECT COUNT(*) as count FROM fact_trading_volumes_hourly"
                results = self.analytics_db.fetch_data(count_query)
                count = results[0]['count'] if results else 0
                
                self.stats['fact_volumes_loaded'] = count
                logger.info(f"Loaded {count} records into fact_trading_volumes_hourly from staging")
                return True
            else:
                logger.error("Failed to execute fact table aggregation query")
                return False
            
        except Exception as e:
            logger.error(f"Failed to load fact_trading_volumes_hourly: {e}", exc_info=True)
            return False
    
    def _get_datetime_keys(self) -> Dict[datetime, int]:
        """Get mapping of datetime to datetime_key"""
        query = "SELECT datetime_key, full_datetime FROM dim_date_time ORDER BY full_datetime"
        results = self.analytics_db.fetch_data(query)
        return {row['full_datetime']: row['datetime_key'] for row in results}
    
    def _get_client_keys(self) -> Dict[int, int]:
        """Get mapping of user_id to client_key"""
        query = "SELECT client_key, user_id FROM dim_clients"
        results = self.analytics_db.fetch_data(query)
        return {row['user_id']: row['client_key'] for row in results}
    
    def _get_instrument_key(self, instrument_id: int) -> Optional[int]:
        """Get instrument_key for given instrument_id"""
        query = "SELECT instrument_key FROM dim_instruments WHERE instrument_id = %s"
        results = self.analytics_db.fetch_data(query, (instrument_id,))
        return results[0]['instrument_key'] if results else None
    
    def _get_account_key(self, account_id: int) -> Optional[int]:
        """Get account_key for given account_id"""
        query = "SELECT account_key FROM dim_accounts WHERE account_id = %s"
        results = self.analytics_db.fetch_data(query, (account_id,))
        return results[0]['account_key'] if results else None
    
    def _print_statistics(self):
        """Print ETL statistics"""
        logger.info("")
        logger.info("-" * 80)
        logger.info("ETL Pipeline Statistics:")
        logger.info("-" * 80)
        for key, value in self.stats.items():
            logger.info(f"  {key:.<50} {value:>10}")
        logger.info("-" * 80)
