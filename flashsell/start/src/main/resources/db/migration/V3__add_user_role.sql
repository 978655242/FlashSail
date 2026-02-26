-- 添加用户角色字段
-- 为 RBAC 权限控制做准备

-- 添加 role 字段到 users 表
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- 添加注释
COMMENT ON COLUMN users.role IS '用户角色: USER-普通用户, ADMIN-管理员';

-- 创建索引以优化角色查询
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 更新现有用户为默认角色
UPDATE users SET role = 'USER' WHERE role IS NULL;
