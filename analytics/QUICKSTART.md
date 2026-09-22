# ETL Pipeline Quick Start Guide

## 5-Minute Setup

### Step 1: Install Python Dependencies
```bash
cd analytics
pip install -r requirements.txt
```

### Step 2: Configure Environment
```bash
cp .env.example .env
# Edit .env with your database credentials
```
```

## Next Steps

1. **Configure .env** with your database credentials
2. **Test connection** - Pipeline will validate on startup
3. **Run pipeline** - `python main.py`
4. **Query results** - Use trading_volumes_daily table for analysis

## Adding More Pipelines

Create new files like `client_activity_etl.py` following the same pattern:
1. Inherit architecture from `trading_volumes_etl.py`
2. Implement extract(), transform(), load() methods
3. Add to `main.py` execution

## Logging

All pipeline activity is logged to:
- **Console**: Real-time feedback
- **File**: `etl_pipeline.log` for record-keeping

Adjust log level in `.env` with `LOG_LEVEL` setting.

