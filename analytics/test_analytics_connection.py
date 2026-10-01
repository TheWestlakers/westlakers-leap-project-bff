"""
Test script to validate analytics database connection
Run this to verify the OLAP/data warehouse database is accessible
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


def test_analytics_connection():
    """Test connection to analytics database"""
    
    logger.info("=" * 70)
    logger.info("ANALYTICS DATABASE CONNECTION TEST")
    logger.info("=" * 70)
    
    # Get analytics database config
    db_config = DatabaseConfig.get_analytics_db_config()
    logger.info(f"\nConnecting to: {db_config['host']}:{db_config['port']}/{db_config['database']}")
    
    # Create connection
    db = DatabaseConnection(db_config, 'analytics')
    
    # Step 1: Test connection
    logger.info("\n[1/4] Testing database connection...")
    if not db.connect():
        logger.error("✗ Failed to connect to analytics database")
        return False
    logger.info("✓ Successfully connected to analytics database")
    
    # Step 2: Test connection health
    logger.info("\n[2/4] Testing connection health...")
    if not db.test_connection():
        logger.error("✗ Connection health test failed")
        db.disconnect()
        return False
    logger.info("✓ Connection health test passed")
    
    # Step 3: Get table list
    logger.info("\n[3/4] Retrieving available tables...")
    tables = db.get_table_list()
    if not tables:
        logger.warning("⚠ No tables found - analytics database may be empty")
        logger.info("   This is expected if the ETL pipeline hasn't run yet")
    else:
        logger.info(f"✓ Found {len(tables)} tables:")
        for table in tables:
            logger.info(f"   - {table}")
            
            # Get table structure
            table_info_query = f"""
            SELECT column_name, data_type 
            FROM information_schema.columns 
            WHERE table_name = '{table}'
            ORDER BY ordinal_position
            """
            columns = db.fetch_data(table_info_query)
            if columns:
                logger.info(f"     Columns ({len(columns)}):")
                for col in columns:
                    logger.info(f"       - {col['column_name']}: {col['data_type']}")
            
            # Get row count
            count_query = f"SELECT COUNT(*) as row_count FROM {table}"
            count_result = db.fetch_data(count_query)
            if count_result:
                row_count = count_result[0]['row_count']
                logger.info(f"     Row count: {row_count}")
                
                # Get sample data if table has rows
                if row_count > 0:
                    sample_query = f"SELECT * FROM {table} LIMIT 3"
                    sample_data = db.fetch_data(sample_query)
                    if sample_data:
                        logger.info(f"     Sample data ({len(sample_data)} records):")
                        for i, record in enumerate(sample_data, 1):
                            logger.info(f"       Record {i}:")
                            for key, value in record.items():
                                logger.info(f"         {key}: {value}")
            
            logger.info("")
    
    # Step 4: Summary
    logger.info("\n[4/4] Connection Summary:")
    logger.info(f"✓ Database: {db_config['database']}")
    logger.info(f"✓ Host: {db_config['host']}:{db_config['port']}")
    logger.info(f"✓ Tables: {len(tables) if tables else 0}")
    
    # Cleanup
    db.disconnect()
    logger.info("\n" + "=" * 70)
    logger.info("TEST COMPLETE")
    logger.info("=" * 70)
    
    return True


def main():
    """Main entry point"""
    try:
        success = test_analytics_connection()
        sys.exit(0 if success else 1)
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
