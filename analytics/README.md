# Analytics ETL Pipeline

This folder contains ETL (Extract, Transform, Load) pipelines for data analysis and business intelligence. The pipelines extract data from the Leap BFF source database and load processed data into an analytics database for stakeholders.

## Overview

### Currently Implemented Pipelines

1. **Trading Volumes ETL** (`trading_volumes_etl.py`)
   - Extracts hourly trading volume data
   - Calculates volume metrics and statistics
   - Identifies trading patterns and trends
   - Tracks unique clients per time period

## Project Structure

```
analytics/
├── config.py                 # Configuration management
├── connection.py             # Database connection handling
├── trading_volumes_etl.py    # Trading volumes pipeline implementation
├── main.py                   # Entry point for pipeline execution
├── requirements.txt          # Python dependencies
├── .env.example              # Example environment variables
└── README.md                 # This file
```

## Setup Instructions

### 1. Install Dependencies

```bash
cd analytics
pip install -r requirements.txt
```

### 2. Configure Environment

Copy the example environment file and update it with your database credentials:

```bash
cp .env.example .env
```

Edit `.env` and provide:
- Source database credentials (Leap BFF database)
- Analytics database credentials (OLAP/data warehouse database)
- ETL configuration settings

Example `.env` file:
```
# Source Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=leap_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Analytics Database
ANALYTICS_DB_HOST=localhost
ANALYTICS_DB_PORT=5432
ANALYTICS_DB_NAME=analytics_db
ANALYTICS_DB_USERNAME=postgres
ANALYTICS_DB_PASSWORD=your_password

# ETL Configuration
BATCH_SIZE=1000
ENABLE_DATA_PROFILING=true
LOG_LEVEL=INFO
```

### 3. Verify Database Connections

Before running pipelines, test that both databases are accessible. The pipeline will perform connection tests on startup.

## Running the Pipeline

### Run All Pipelines

```bash
python main.py
```

### Run Specific Pipeline

```bash
from trading_volumes_etl import TradingVolumesETL

pipeline = TradingVolumesETL()
success = pipeline.run(days_back=30)
```

## Configuration

### ETL Configuration (`config.py`)

- **BATCH_SIZE**: Number of records to process in each batch (default: 1000)
- **ENABLE_DATA_PROFILING**: Enable data quality statistics logging (default: true)
- **LOG_LEVEL**: Logging verbosity - DEBUG, INFO, WARNING, ERROR (default: INFO)
- **LOG_FILE**: Path to log file (default: etl_pipeline.log)

## Trading Volumes Pipeline

### What It Does

1. **Extract**: Retrieves hourly trading data from source database for specified period
2. **Transform**: Cleans data, calculates metrics, validates data quality
3. **Load**: Inserts/updates records in analytics database

### Data Extracted

- Trade date and hour
- Trade count per hour
- Total volume, average/min/max prices
- Unique client count
- Volume per trade and price range metrics

### Output Table

**trading_volumes_daily** table in analytics database:
- Hourly aggregated trading volumes
- Price statistics
- Client activity metrics
- Supports UPSERT to handle data updates

### Features

- **Batch Processing**: Handles large datasets efficiently
- **Data Validation**: Automatic data type conversion and null handling
- **Metrics Calculation**: Derived metrics (volume per trade, price range)
- **Data Profiling**: Optional statistical summary of extracted data
- **Error Handling**: Comprehensive logging and error recovery
- **Duplicate Handling**: UPSERT logic to update existing records

## Logging

Logs are written to both file and console (stdout). Default log level is INFO.

Log file location: `etl_pipeline.log` (in the analytics folder)

Log entries include:
- Timestamps
- Component/module name
- Log level (DEBUG, INFO, WARNING, ERROR)
- Detailed messages

## Future Pipelines

The architecture supports adding additional pipelines for:
- Most active instruments analysis
- Client activity trends
- Market statistics
- Custom business metrics

To add a new pipeline:
1. Create a new file (e.g., `client_activity_etl.py`)
2. Implement similar Extract, Transform, Load methods
3. Add pipeline execution to `main.py`

## Database Requirements

### Source Database (Leap BFF)

Requires access to trading data with:
- `trades` table or equivalent
- Fields: `created_at`, `volume`, `price`, `client_id`

### Analytics Database

- Should be on same PostgreSQL server (or accessible)
- Will automatically create required tables
- Requires UPSERT support (PostgreSQL 9.5+)

## Troubleshooting

### Connection Failures

1. Verify database credentials in `.env`
2. Check database server is running
3. Ensure network connectivity
4. Check database user permissions

### Missing Tables

If the source database doesn't have expected tables:
- Verify the correct database is specified in `.env`
- Check table names in SQL queries match your schema
- Update queries in `trading_volumes_etl.py` as needed

### Performance Issues

- Adjust `BATCH_SIZE` in `.env`
- Run pipeline during off-peak hours
- Consider indexing on analytics tables
- Enable data profiling only when needed

## Support

For issues or questions:
1. Check log files for error details
2. Verify database connectivity
3. Ensure all dependencies are installed
4. Review configuration settings

