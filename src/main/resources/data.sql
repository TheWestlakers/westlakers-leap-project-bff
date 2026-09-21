INSERT INTO roles (role_id, role_name) VALUES (1, 'ADMIN');
INSERT INTO roles (role_id, role_name) VALUES (2, 'USER');

-- Insert User Status (explicit IDs to ensure consistency)
INSERT INTO user_status (user_status_id, status_name, description) VALUES (1, 'ONLINE', 'User account is active');
INSERT INTO user_status (user_status_id, status_name, description) VALUES (2, 'OFFLINE', 'User account is inactive');

-- Insert Account Status (explicit IDs to ensure consistency)
INSERT INTO account_status (account_status_id, status_name, description) VALUES (1, 'OPEN', 'Account is open and active');
INSERT INTO account_status (account_status_id, status_name, description) VALUES (2, 'CLOSED', 'Account is closed');
INSERT INTO account_status (account_status_id, status_name, description) VALUES (3, 'FROZEN', 'Account is frozen');

-- Insert Account Types (explicit IDs to ensure consistency)
INSERT INTO account_types (account_type_id, type_name) VALUES (1, 'INDIVIDUAL');
INSERT INTO account_types (account_type_id, type_name) VALUES (2, 'HEALTH');
INSERT INTO account_types (account_type_id, type_name) VALUES (3, 'RETIREMENT');
INSERT INTO account_types (account_type_id, type_name) VALUES (4, 'CORPORATE');

-- Insert Order Status (explicit IDs to ensure consistency)
INSERT INTO order_status (order_status_id, status_name, description) VALUES (1, 'PENDING', 'Order pending execution');
INSERT INTO order_status (order_status_id, status_name, description) VALUES (2, 'EXECUTED', 'Order fully executed');
INSERT INTO order_status (order_status_id, status_name, description) VALUES (3, 'CANCELLED', 'Order cancelled');
INSERT INTO order_status (order_status_id, status_name, description) VALUES (4, 'REJECTED', 'Order rejected');

-- Insert Asset Classes (explicit IDs to ensure consistency)
INSERT INTO asset_classes (asset_class_id, class_name) VALUES (1, 'EQUITY');
INSERT INTO asset_classes (asset_class_id, class_name) VALUES (2, 'FOREX');
INSERT INTO asset_classes (asset_class_id, class_name) VALUES (3, 'CRYPTO');

-- Insert Markets (explicit IDs to ensure consistency)
INSERT INTO markets (market_id, market_name, market_code, timezone, open_time, close_time) 
VALUES (1, 'New York Stock Exchange', 'NYSE', 'America/New_York', '09:30:00', '16:00:00');
INSERT INTO markets (market_id, market_name, market_code, timezone, open_time, close_time) 
VALUES (2, 'NASDAQ', 'NASDAQ', 'America/New_York', '09:30:00', '16:00:00');
INSERT INTO markets (market_id, market_name, market_code, timezone, open_time, close_time) 
VALUES (3, 'London Stock Exchange', 'LSE', 'Europe/London', '08:00:00', '16:30:00');
INSERT INTO markets (market_id, market_name, market_code, timezone, open_time, close_time) 
VALUES (4, 'Tokyo Stock Exchange', 'TSE', 'Asia/Tokyo', '09:00:00', '15:00:00');






