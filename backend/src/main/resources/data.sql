-- 默认管理员账号初始化
-- 用户名: admin
-- 邮箱: admin@example.com
-- 密码: 123456 (BCrypt加密)
INSERT INTO users (username, email, password, balance, status, email_verified, created_at)
SELECT 'admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 1000000, 'active', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
