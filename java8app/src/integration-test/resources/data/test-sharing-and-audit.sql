-- Test data for document_categories table
INSERT INTO document_categories (id, name, description, owner_id, color_code, created_at, updated_at, is_active)
VALUES
    (1, 'Projects', 'Active project documents', 1, '#FF5733', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (2, 'Finance', 'Financial and budget documents', 1, '#33FF57', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (3, 'Internal', 'Internal team documents', 1, '#3357FF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (4, 'Archived', 'Old archived documents', 1, '#999999', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (5, 'Technical', 'Technical specifications', 2, '#FF33F5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (6, 'Training', 'Training and onboarding', 3, '#F5FF33', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Advance identity sequence so subsequently saved entities don't collide with these fixed IDs
ALTER TABLE document_categories ALTER COLUMN id RESTART WITH 100;

-- Test data for document_shares table
-- Alice shares document 1 with Bob (EDIT permission)
-- Note: document 2 is intentionally left unshared so sharing-workflow tests can create fresh shares on it
INSERT INTO document_shares (id, document_id, shared_by_user_id, shared_with_user_id, permission_type, shared_at, expires_at, is_active)
VALUES
    (1, 1, 1, 2, 'EDIT', CURRENT_TIMESTAMP, NULL, true),
    (2, 1, 1, 3, 'VIEW', CURRENT_TIMESTAMP, NULL, true),
    (3, 7, 3, 4, 'VIEW', CURRENT_TIMESTAMP, NULL, true),
    (4, 5, 2, 1, 'EDIT', CURRENT_TIMESTAMP, NULL, true),
    (5, 6, 2, 1, 'VIEW', CURRENT_TIMESTAMP, NULL, true),
    (6, 7, 3, 2, 'EDIT', CURRENT_TIMESTAMP, NULL, true),
    (7, 1, 1, 5, 'VIEW', CURRENT_TIMESTAMP, DATEADD('DAY', 7, CURRENT_TIMESTAMP), true),
    (8, 8, 3, 2, 'EDIT', CURRENT_TIMESTAMP, NULL, false);

-- Advance identity sequence so subsequently saved entities don't collide with these fixed IDs
ALTER TABLE document_shares ALTER COLUMN id RESTART WITH 100;

-- Test data for document_access_logs table
INSERT INTO document_access_logs (id, document_id, user_id, action_type, accessed_at, ip_address, user_agent, details)
VALUES
    (1, 1, 1, 'VIEW', CURRENT_TIMESTAMP, '192.168.1.1', 'Mozilla/5.0 Chrome', 'Owner viewed the document'),
    (2, 1, 2, 'EDIT', DATEADD('MINUTE', -5, CURRENT_TIMESTAMP), '192.168.1.2', 'Mozilla/5.0 Firefox', 'Bob edited content'),
    (3, 1, 1, 'SHARE', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), '192.168.1.1', 'Mozilla/5.0 Chrome', 'Shared with user 3'),
    (4, 2, 1, 'DOWNLOAD', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), '192.168.1.1', 'Mozilla/5.0 Chrome', 'Downloaded budget report'),
    (5, 3, 1, 'VIEW', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), '192.168.1.1', 'Mozilla/5.0 Chrome', 'Owner viewed notes'),
    (6, 5, 2, 'VIEW', DATEADD('DAY', -1, CURRENT_TIMESTAMP), '192.168.1.2', 'Mozilla/5.0 Firefox', 'Owner viewed tech spec'),
    (7, 5, 1, 'VIEW', DATEADD('DAY', -1, CURRENT_TIMESTAMP), '192.168.1.3', 'Mozilla/5.0 Safari', 'Alice reviewed tech spec'),
    (8, 7, 3, 'EDIT', DATEADD('DAY', -2, CURRENT_TIMESTAMP), '192.168.1.4', 'Mozilla/5.0 Chrome', 'Owner updated training materials'),
    (9, 7, 2, 'VIEW', DATEADD('DAY', -2, CURRENT_TIMESTAMP), '192.168.1.2', 'Mozilla/5.0 Firefox', 'Bob accessed training materials');

-- Advance identity sequence so subsequently saved entities don't collide with these fixed IDs
ALTER TABLE document_access_logs ALTER COLUMN id RESTART WITH 100;
