-- Phase 3: Task Management Schema
-- Run this SQL to create the tasks table

CREATE SEQUENCE IF NOT EXISTS seq_tasks START 1;

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_tasks'),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL,
    assigned_role VARCHAR(50),
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_role ON tasks(assigned_role);
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON tasks(created_by);

-- Sample seed data for testing (optional)
-- INSERT INTO tasks (title, description, status, priority, assigned_role, created_at, updated_at)
-- VALUES ('Review pending orders', 'Check and process all pending orders', 'TODO', 'HIGH', 'ADMIN', NOW(), NOW());
