"""
Database connection management and utilities
"""
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
from typing import Optional, List, Dict, Any
from config import DatabaseConfig

logger = logging.getLogger(__name__)


class DatabaseConnection:
    """Manages database connections for source and analytics databases"""
    
    def __init__(self, db_config: Dict[str, Any], db_type: str = 'source'):
        """
        Initialize database connection
        
        Args:
            db_config: Database configuration dictionary
            db_type: Type of database ('source' or 'analytics')
        """
        self.db_config = db_config
        self.db_type = db_type
        self.connection: Optional[psycopg2.extensions.connection] = None
    
    def connect(self) -> bool:
        """
        Establish database connection
        
        Returns:
            bool: True if connection successful, False otherwise
        """
        try:
            self.connection = psycopg2.connect(**self.db_config)
            logger.info(f"Successfully connected to {self.db_type} database")
            return True
        except psycopg2.Error as e:
            logger.error(f"Failed to connect to {self.db_type} database: {e}")
            return False
    
    def disconnect(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()
            logger.info(f"Disconnected from {self.db_type} database")
    
    def test_connection(self) -> bool:
        """
        Test database connection and return status
        
        Returns:
            bool: True if connection is healthy, False otherwise
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT 1")
            cursor.close()
            logger.info(f"{self.db_type.capitalize()} database connection test passed")
            return True
        except psycopg2.Error as e:
            logger.error(f"{self.db_type.capitalize()} database connection test failed: {e}")
            return False
    
    def execute_query(self, query: str, params: tuple = None) -> bool:
        """
        Execute a query (INSERT, UPDATE, DELETE)
        
        Args:
            query: SQL query string
            params: Query parameters
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            cursor = self.connection.cursor()
            cursor.execute(query, params or ())
            self.connection.commit()
            logger.debug(f"Query executed successfully. Rows affected: {cursor.rowcount}")
            cursor.close()
            return True
        except psycopg2.Error as e:
            self.connection.rollback()
            logger.error(f"Query execution failed: {e}")
            return False
    
    def fetch_data(self, query: str, params: tuple = None) -> List[Dict[str, Any]]:
        """
        Fetch data from database
        
        Args:
            query: SQL query string
            params: Query parameters
            
        Returns:
            List of dictionaries containing row data
        """
        try:
            cursor = self.connection.cursor(cursor_factory=RealDictCursor)
            cursor.execute(query, params or ())
            results = cursor.fetchall()
            cursor.close()
            logger.debug(f"Fetched {len(results)} rows from database")
            return results
        except psycopg2.Error as e:
            logger.error(f"Data fetch failed: {e}")
            return []
    
    def get_table_list(self) -> List[str]:
        """
        Get list of tables in the database
        
        Returns:
            List of table names
        """
        query = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public'
            ORDER BY table_name
        """
        try:
            results = self.fetch_data(query)
            tables = [row['table_name'] for row in results]
            logger.info(f"Found {len(tables)} tables in database")
            return tables
        except Exception as e:
            logger.error(f"Failed to retrieve table list: {e}")
            return []
