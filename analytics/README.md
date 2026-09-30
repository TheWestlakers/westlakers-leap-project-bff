# Analytics ETL Pipeline

Extract, transform, and load trading data from the source database into an analytics warehouse using a master data table architecture.

## Quick Start

### 1. Install Dependencies
```bash
pip install -r requirements.txt
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env with your database credentials
```

### 3. Run Pipelines
```bash
# Run all pipelines
python main.py

# Run specific pipeline
python main.py --pipeline trading_volumes --days-back 30
python main.py --pipeline active_instruments --days-back 30
python main.py --pipeline client_activity_trends --days-back 30
python main.py --pipeline client_segment_analysis --days-back 30
```

## Pipelines

1. **Trading Volumes ETL** - Extracts orders, loads staging table, generates fact table
2. **Active Instruments** - Daily instrument activity analysis
3. **Client Activity Trends** - Daily client engagement metrics
4. **Client Segment Analysis** - Daily segment performance

## Architecture

- **stg_trading_volumes** - Master staging table (denormalized raw data)
- **fact_trading_volumes_hourly** - Master fact table (hourly aggregations)
- **dim_*** tables** - Dimensions (date, clients, instruments, accounts)
- **agg_*** tables** - Derived aggregations (daily summaries)

All aggregations derive from the master fact table, providing a single source of truth.

## Configuration

Set in `.env`:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` - Source database
- `ANALYTICS_DB_HOST`, `ANALYTICS_DB_PORT`, `ANALYTICS_DB_NAME`, `ANALYTICS_DB_USERNAME`, `ANALYTICS_DB_PASSWORD` - Analytics database
- `LOG_LEVEL` - DEBUG, INFO, WARNING, ERROR (default: INFO)
- `LOG_FILE` - Log file path (default: etl_pipeline.log)

## Logs

See `etl_pipeline.log` for execution details and errors.

