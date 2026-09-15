INSERT INTO roles (role_name)
VALUES
    ('Admin'),
    ('User')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO user_status (status_name, description)
VALUES
    ('Active', 'Active User'),
    ('Inactive', 'Inactive User')
ON CONFLICT (status_name) DO NOTHING;

INSERT INTO account_status (status_name, description)
VALUES
    ('Active', 'Active Account'),
    ('Suspended', 'Suspended Account'),
    ('Closed', 'Closed Account')
ON CONFLICT (status_name) DO NOTHING;

INSERT INTO account_types (type_name)
VALUES
    ('Brokerage'),
    ('Retirement'),
    ('Cash')
ON CONFLICT (type_name) DO NOTHING;

INSERT INTO order_status (status_name, description)
VALUES
    ('Pending', 'Pending execution'),
    ('Executed', 'Order has been executed'),
    ('Cancelled', 'Order has been cancelled')
ON CONFLICT (status_name) DO NOTHING;



