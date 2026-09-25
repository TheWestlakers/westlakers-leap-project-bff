-- ============================================================================
-- DIMENSION TABLES
-- ============================================================================

-- Time dimension (supports all period analysis)
CREATE TABLE IF NOT EXISTS dim_date_time (
    datetime_key SERIAL PRIMARY KEY,
    full_datetime TIMESTAMP NOT NULL UNIQUE,
    date DATE,
    year INT,
    quarter INT,
    month INT,
    day INT,
    hour INT,
    day_of_week VARCHAR(10),
    week_of_year INT,
    is_trading_day BOOLEAN
);

-- Instrument dimension
CREATE TABLE IF NOT EXISTS dim_instruments (
    instrument_key SERIAL PRIMARY KEY,
    instrument_id INT NOT NULL UNIQUE,
    instrument_code VARCHAR(20),
    instrument_name VARCHAR(255),
    instrument_type VARCHAR(50),
    created_date TIMESTAMP
);

-- User/Client dimension
CREATE TABLE IF NOT EXISTS dim_clients (
    client_key SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    client_segment VARCHAR(50),
    created_date TIMESTAMP,
    updated_date TIMESTAMP
);

-- Account dimension
CREATE TABLE IF NOT EXISTS dim_accounts (
    account_key SERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE,
    client_key INT REFERENCES dim_clients(client_key),
    account_type VARCHAR(50),
    currency VARCHAR(3),
    created_date TIMESTAMP
);

-- ============================================================================
-- FACT/AGGREGATE TABLES
-- ============================================================================

-- Trading volumes by hour, instrument, and account
CREATE TABLE IF NOT EXISTS fact_trading_volumes_hourly (
    trading_volume_key SERIAL PRIMARY KEY,
    datetime_key INT REFERENCES dim_date_time(datetime_key),
    instrument_key INT REFERENCES dim_instruments(instrument_key),
    account_key INT REFERENCES dim_accounts(account_key),
    client_key INT REFERENCES dim_clients(client_key),
    trade_count INT DEFAULT 0,
    total_volume DECIMAL(18,8),
    total_value DECIMAL(18,2),
    avg_price DECIMAL(18,8),
    min_price DECIMAL(18,8),
    max_price DECIMAL(18,8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(datetime_key, instrument_key, account_key)
);

-- Most active instruments aggregate
CREATE TABLE IF NOT EXISTS agg_instrument_activity (
    instrument_activity_key SERIAL PRIMARY KEY,
    date_key INT,
    instrument_key INT REFERENCES dim_instruments(instrument_key),
    total_trades INT,
    total_volume DECIMAL(18,8),
    total_value DECIMAL(18,2),
    unique_accounts INT,
    unique_clients INT,
    avg_trade_value DECIMAL(18,2),
    activity_rank INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(date_key, instrument_key)
);

-- Client activity trends
CREATE TABLE IF NOT EXISTS agg_client_activity_daily (
    client_activity_key SERIAL PRIMARY KEY,
    date_key INT,
    client_key INT REFERENCES dim_clients(client_key),
    client_segment VARCHAR(50),
    account_count INT,
    total_trades INT,
    total_volume DECIMAL(18,8),
    total_value DECIMAL(18,2),
    unique_instruments INT,
    avg_trade_value DECIMAL(18,2),
    trading_days_active INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(date_key, client_key)
);

-- Client segment summary
CREATE TABLE IF NOT EXISTS agg_segment_activity_daily (
    segment_activity_key SERIAL PRIMARY KEY,
    date_key INT,
    client_segment VARCHAR(50),
    client_count INT,
    account_count INT,
    total_trades INT,
    total_volume DECIMAL(18,8),
    total_value DECIMAL(18,2),
    unique_instruments INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(date_key, client_segment)
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_fact_trading_volumes_datetime ON fact_trading_volumes_hourly(datetime_key);
CREATE INDEX IF NOT EXISTS idx_fact_trading_volumes_instrument ON fact_trading_volumes_hourly(instrument_key);
CREATE INDEX IF NOT EXISTS idx_fact_trading_volumes_client ON fact_trading_volumes_hourly(client_key);
CREATE INDEX IF NOT EXISTS idx_agg_instrument_date ON agg_instrument_activity(date_key);
CREATE INDEX IF NOT EXISTS idx_agg_client_date ON agg_client_activity_daily(date_key);
CREATE INDEX IF NOT EXISTS idx_agg_segment_date ON agg_segment_activity_daily(date_key);
CREATE INDEX IF NOT EXISTS idx_dim_date_date ON dim_date_time(date);
CREATE INDEX IF NOT EXISTS idx_dim_clients_segment ON dim_clients(client_segment);