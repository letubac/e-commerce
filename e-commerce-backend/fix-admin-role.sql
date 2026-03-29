-- Fix admin user role for chat admin endpoints access
-- Update all users with admin emails to have ADMIN role

UPDATE users SET role = 'ADMIN' WHERE email IN ('admin@ecommerce.com', 'admin@gmail.com', 'manager@eshop.com');

-- Verify the update
SELECT id, email, role, is_active FROM users WHERE email IN ('admin@ecommerce.com', 'admin@gmail.com', 'manager@eshop.com');

-- If using user_roles table, ensure admin role exists and is assigned
-- First, check if roles table exists
-- INSERT INTO roles (id, name) VALUES (2, 'ADMIN') ON CONFLICT DO NOTHING;

-- Assign ADMIN role to admin users (if using user_roles table)
-- INSERT INTO user_roles (user_id, role_id)
-- SELECT u.id, 2 FROM users u WHERE u.email IN ('admin@ecommerce.com', 'admin@gmail.com', 'manager@eshop.com')
-- ON CONFLICT (user_id, role_id) DO NOTHING;
