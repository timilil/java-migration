-- Test data for documents table
-- Documents owned by Alice (user 1)
INSERT INTO documents (id, title, description, owner_id, content_type, file_size, storage_path, created_at, updated_at, is_active)
VALUES
    (1, 'Project Plan', 'Q3 project planning document', 1, 'application/pdf', 2048, '/storage/project-plan.pdf', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (2, 'Budget Report', 'Annual budget for 2026', 1, 'application/vnd.ms-excel', 1024, '/storage/budget.xlsx', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (3, 'Meeting Notes', 'Notes from team meeting', 1, 'text/plain', 512, '/storage/notes.txt', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (4, 'Archived Document', 'Old document', 1, 'application/pdf', 800, '/storage/archived.pdf', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- Documents owned by Bob (user 2)
INSERT INTO documents (id, title, description, owner_id, content_type, file_size, storage_path, created_at, updated_at, is_active)
VALUES
    (5, 'Technical Specification', 'System architecture document', 2, 'application/pdf', 3072, '/storage/tech-spec.pdf', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (6, 'API Documentation', 'REST API endpoints', 2, 'text/html', 1536, '/storage/api-docs.html', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Documents owned by Charlie (user 3)
INSERT INTO documents (id, title, description, owner_id, content_type, file_size, storage_path, created_at, updated_at, is_active)
VALUES
    (7, 'Training Materials', 'New employee training guide', 3, 'application/pdf', 4096, '/storage/training.pdf', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    (8, 'Process Documentation', 'Standard operating procedures', 3, 'text/plain', 2560, '/storage/process.txt', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Advance identity sequence so subsequently saved entities don't collide with these fixed IDs
ALTER TABLE documents ALTER COLUMN id RESTART WITH 100;
