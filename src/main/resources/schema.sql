CREATE TABLE IF NOT EXISTS roles (
                       role_id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_status (
                             user_status_id SERIAL PRIMARY KEY,
                             status_name VARCHAR(50) NOT NULL UNIQUE,
                             description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS account_status (
                                account_status_id SERIAL PRIMARY KEY,
                                status_name VARCHAR(50) NOT NULL UNIQUE,
                                description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS account_types (
                               account_type_id SERIAL PRIMARY KEY,
                               type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS order_status (
                              order_status_id SERIAL PRIMARY KEY,
                              status_name VARCHAR(50) NOT NULL UNIQUE,
                              description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS asset_classes (
                            asset_class_id SERIAL PRIMARY KEY,
                            class_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS markets (
                         market_id SERIAL PRIMARY KEY,
                         market_name VARCHAR(100) NOT NULL,
                         market_code VARCHAR(20) NOT NULL UNIQUE,
                         timezone VARCHAR(50),
                         open_time TIME,
                         close_time TIME
);

CREATE TABLE IF NOT EXISTS users (
                       user_id SERIAL PRIMARY KEY,
                       date_of_birth DATE NOT NULL,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       phone_number VARCHAR(20) NOT NULL UNIQUE,
                       status_id INTEGER NOT NULL,
                       create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       tax_id VARCHAR(20) UNIQUE,

                       CONSTRAINT fk_users_status
                           FOREIGN KEY (status_id)
                               REFERENCES user_status(user_status_id)
);

CREATE TABLE IF NOT EXISTS user_credentials (
                                  credential_id SERIAL PRIMARY KEY,
                                  user_id INTEGER NOT NULL,
                                  role_id INTEGER NOT NULL,
                                  email VARCHAR(255) UNIQUE NOT NULL,
                                  username VARCHAR(50) UNIQUE NOT NULL,
                                  password_hash VARCHAR(255) NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_credential_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(user_id),

                                  CONSTRAINT fk_credential_role
                                      FOREIGN KEY (role_id)
                                          REFERENCES roles(role_id)
);

CREATE TABLE IF NOT EXISTS accounts (
                          account_id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL,
                          account_type_id INTEGER NOT NULL,
                          account_status_id INTEGER NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          settled_cash NUMERIC(19,4) DEFAULT 0.00,

                          CONSTRAINT fk_account_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(user_id),

                          CONSTRAINT fk_account_status
                              FOREIGN KEY (account_status_id)
                                  REFERENCES account_status(account_status_id),

                          CONSTRAINT fk_account_type
                              FOREIGN KEY (account_type_id)
                                  REFERENCES account_types(account_type_id)
);

CREATE TABLE IF NOT EXISTS instruments (
                             instrument_id SERIAL PRIMARY KEY,
                             market_id INTEGER NOT NULL,
                             asset_class_id INTEGER NOT NULL,
                             ticker VARCHAR(20) NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL,

                             CONSTRAINT fk_instrument_market
                                 FOREIGN KEY (market_id)
                                     REFERENCES markets(market_id),
                             CONSTRAINT fk_instrument_asset_class
                                 FOREIGN KEY (asset_class_id)
                                     REFERENCES asset_classes(asset_class_id)
);

CREATE TABLE IF NOT EXISTS orders (
                        order_id SERIAL PRIMARY KEY,
                        account_id INTEGER NOT NULL,
                        instrument_id INTEGER NOT NULL,
                        side VARCHAR(10) NOT NULL,
                        order_type VARCHAR(20) NOT NULL,
                        limit_price NUMERIC(19,4),
                        quantity DECIMAL(19,4) NOT NULL,
                        status INTEGER NOT NULL,
                        placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_order_account
                            FOREIGN KEY (account_id)
                                REFERENCES accounts(account_id),

                        CONSTRAINT fk_order_instrument
                            FOREIGN KEY (instrument_id)
                                REFERENCES instruments(instrument_id),

                        CONSTRAINT fk_order_status
                            FOREIGN KEY (status)
                                REFERENCES order_status(order_status_id)
);


CREATE TABLE IF NOT EXISTS holdings (
                          holding_id SERIAL PRIMARY KEY,
                          account_id INTEGER NOT NULL,
                          instrument_id INTEGER NOT NULL,
                          quantity DECIMAL(19,4) NOT NULL,
                          average_price NUMERIC(19,4) NOT NULL,

                          CONSTRAINT fk_holding_account
                              FOREIGN KEY (account_id)
                                  REFERENCES accounts(account_id),

                          CONSTRAINT fk_holding_instrument
                              FOREIGN KEY (instrument_id)
                                  REFERENCES instruments(instrument_id)
);
