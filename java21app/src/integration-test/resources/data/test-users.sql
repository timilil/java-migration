-- Test data for users table
INSERT INTO users (id, username, email, full_name, password, created_at, updated_at, active)
VALUES
    (1, 'alice', 'alice@example.com', 'Alice Anderson', 'password123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (2, 'bob', 'bob@example.com', 'Bob Brown', 'password123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (3, 'charlie', 'charlie@example.com', 'Charlie Clark', 'password123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (4, 'diana', 'diana@example.com', 'Diana Davis', 'password123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (5, 'eve', 'eve@example.com', 'Eve Evans', 'password123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Advance identity sequence so subsequently saved entities don't collide with these fixed IDs
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
