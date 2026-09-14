INSERT INTO roles (role_name)
VALUES
    ('Admin'),
    ('User');

INSERT INTO user_status (status_name, description)
VALUES
    ('Active', 'Active User'),
    ('Inactive', 'Inactive User');

INSERT INTO account_status (status_name, description)
VALUES
    ('Active', 'Active Account'),
    ('Suspended', 'Suspended Account'),
    ('Closed', 'Closed Account');

INSERT INTO order_status (status_name, description)
VALUES
    ('Pending', 'Pending execution'),
    ('Executed', 'Order has been executed'),
    ('Cancelled', 'Order has been cancelled');



