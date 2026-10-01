"""
Test script to validate source database connection and data availability
Run this before setting up the analytics database to verify data extraction works
"""
import sys
import logging
from connection import DatabaseConnection
from config import DatabaseConfig

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


def test_source_connection():
    """Test connection to source database and display available data"""
    
    logger.info("=" * 70)
    logger.info("SOURCE DATABASE CONNECTION TEST")
    logger.info("=" * 70)
    
    # Get source database config
    db_config = DatabaseConfig.get_source_db_config()
    logger.info(f"\nConnecting to: {db_config['host']}:{db_config['port']}/{db_config['database']}")
    
    # Create connection
    db = DatabaseConnection(db_config, 'source')
    
    # Step 1: Test connection
    logger.info("\n[1/5] Testing database connection...")
    if not db.connect():
        logger.error("✗ Failed to connect to source database")
        return False
    logger.info("✓ Successfully connected to source database")
    
    # Step 2: Test connection health
    logger.info("\n[2/5] Testing connection health...")
    if not db.test_connection():
        logger.error("✗ Connection health test failed")
        db.disconnect()
        return False
    logger.info("✓ Connection health test passed")
    
    # Step 3: Get table list
    logger.info("\n[3/5] Retrieving available tables...")
    tables = db.get_table_list()
    if not tables:
        logger.warning("⚠ No tables found or unable to retrieve table list")
    else:
        logger.info(f"✓ Found {len(tables)} tables:")
        for table in tables:
            logger.info(f"   - {table}")
    
    # Step 4: Check for orders table
    logger.info("\n[4/5] Checking for 'orders' table (required for trading volumes pipeline)...")
    if 'orders' in tables:
        logger.info("✓ 'orders' table found")
        
        # Get orders table info
        table_info_query = """
        SELECT column_name, data_type 
        FROM information_schema.columns 
        WHERE table_name = 'orders'
        ORDER BY ordinal_position
        """
        columns = db.fetch_data(table_info_query)
        if columns:
            logger.info(f"✓ 'orders' table has {len(columns)} columns:")
            for col in columns:
                logger.info(f"   - {col['column_name']}: {col['data_type']}")
        
        # Get row count and sample data
        count_query = "SELECT COUNT(*) as row_count FROM orders"
        count_result = db.fetch_data(count_query)
        if count_result:
            row_count = count_result[0]['row_count']
            logger.info(f"✓ 'orders' table contains {row_count} rows")
            
            if row_count > 0:
                logger.info("\n[5/5] Fetching sample data from 'orders' table...")
                sample_query = "SELECT * FROM orders LIMIT 5"
                sample_data = db.fetch_data(sample_query)
                if sample_data:
                    logger.info(f"✓ Retrieved {len(sample_data)} sample records:")
                    for i, record in enumerate(sample_data, 1):
                        logger.info(f"\n   Record {i}:")
                        for key, value in record.items():
                            logger.info(f"      {key}: {value}")
                else:
                    logger.warning("⚠ Could not retrieve sample data")
            else:
                logger.warning("⚠ 'orders' table is empty - no data to extract")
        else:
            logger.warning("⚠ Could not retrieve row count from 'orders' table")
    else:
        logger.warning("⚠ 'orders' table not found")
        logger.info("   Available tables: " + ", ".join(tables) if tables else "None")
        logger.info("   Note: Check your schema - the pipeline expects an 'orders' table")
        logger.info("   You may need to update the SQL queries in trading_volumes_etl.py")
    
    # Step 5: Test extraction query
    logger.info("\n[5/5] Testing trading volumes extraction query (last 7 days)...")
    extraction_query = """
    SELECT 
        DATE(placed_at) as trade_date,
        EXTRACT(HOUR FROM placed_at) as hour,
        COUNT(*) as trade_count,
        COALESCE(SUM(quantity), 0) as total_volume,
        COALESCE(AVG(limit_price), 0) as avg_price,
        COALESCE(MAX(limit_price), 0) as max_price,
        COALESCE(MIN(limit_price), 0) as min_price,
        COUNT(DISTINCT account_id) as unique_clients
    FROM orders
    WHERE placed_at >= NOW() - INTERVAL '7 days'
    GROUP BY DATE(placed_at), EXTRACT(HOUR FROM placed_at)
    ORDER BY trade_date DESC, hour DESC
    LIMIT 10
    """
    
    try:
        extraction_results = db.fetch_data(extraction_query)
        if extraction_results:
            logger.info(f"✓ Extraction query successful! Retrieved {len(extraction_results)} aggregated records:")
            for i, record in enumerate(extraction_results, 1):
                logger.info(f"\n   Record {i}:")
                for key, value in record.items():
                    logger.info(f"      {key}: {value}")
        else:
            logger.warning("⚠ Extraction query returned no results (no data for last 7 days)")
    except Exception as e:
        logger.error(f"✗ Extraction query failed: {e}")
        logger.info("   This may be due to missing columns. Check your 'orders' table schema:")
        logger.info("   Required columns: placed_at, quantity, limit_price, account_id")
    
    # Cleanup
    db.disconnect()
    logger.info("\n" + "=" * 70)
    logger.info("TEST COMPLETE")
    logger.info("=" * 70)
    
    return True


def main():
    """Main entry point"""
    try:
        success = test_source_connection()
        sys.exit(0 if success else 1)
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
