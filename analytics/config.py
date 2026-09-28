"""
Configuration management for ETL pipeline
Handles environment variables and database connection settings
"""
import os
from dotenv import load_dotenv
from typing import Dict, Any

# Load environment variables from .env file
load_dotenv()


class DatabaseConfig:
    """Source database configuration"""
    
    @staticmethod
    def get_source_db_config() -> Dict[str, Any]:
        """Get source database (Leap BFF) configuration"""
        return {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 5432)),
            'database': os.getenv('DB_NAME', 'leap_db'),
            'user': os.getenv('DB_USERNAME', 'postgres'),
            'password': os.getenv('DB_PASSWORD', 'password'),
        }
    
    @staticmethod
    def get_analytics_db_config() -> Dict[str, Any]:
        """Get analytics database (OLAP) configuration"""
        return {
            'host': os.getenv('ANALYTICS_DB_HOST', 'localhost'),
            'port': int(os.getenv('ANALYTICS_DB_PORT', 5433)),
            'database': os.getenv('ANALYTICS_DB_NAME', 'analytics_db'),
            'user': os.getenv('ANALYTICS_DB_USERNAME', 'postgres'),
            'password': os.getenv('ANALYTICS_DB_PASSWORD', 'password'),
        }


class ETLConfig:
    """ETL pipeline configuration"""
    
    BATCH_SIZE = int(os.getenv('BATCH_SIZE', 1000))
    ENABLE_DATA_PROFILING = os.getenv('ENABLE_DATA_PROFILING', 'true').lower() == 'true'
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
    LOG_FILE = os.getenv('LOG_FILE', 'etl_pipeline.log')
