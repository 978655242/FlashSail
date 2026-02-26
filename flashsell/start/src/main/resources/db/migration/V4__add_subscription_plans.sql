-- 添加订阅套餐表
-- 用于存储不同等级的订阅套餐信息

-- 创建订阅套餐表
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    duration_days INTEGER NOT NULL DEFAULT 30,
    search_limit INTEGER DEFAULT -1,  -- -1 表示无限制
    export_limit INTEGER DEFAULT -1,  -- -1 表示无限制
    board_limit INTEGER DEFAULT -1,   -- -1 表示无限制
    ai_analysis_enabled BOOLEAN DEFAULT true,
    api_access_enabled BOOLEAN DEFAULT false,
    level VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加注释
COMMENT ON TABLE subscription_plans IS '订阅套餐表';
COMMENT ON COLUMN subscription_plans.id IS '套餐ID';
COMMENT ON COLUMN subscription_plans.name IS '套餐名称';
COMMENT ON COLUMN subscription_plans.description IS '套餐描述';
COMMENT ON COLUMN subscription_plans.price IS '价格（人民币）';
COMMENT ON COLUMN subscription_plans.duration_days IS '订阅时长（天）';
COMMENT ON COLUMN subscription_plans.search_limit IS '搜索次数限制，-1表示无限制';
COMMENT ON COLUMN subscription_plans.export_limit IS '导出次数限制，-1表示无限制';
COMMENT ON COLUMN subscription_plans.board_limit IS '看板数量限制，-1表示无限制';
COMMENT ON COLUMN subscription_plans.ai_analysis_enabled IS '是否支持AI分析';
COMMENT ON COLUMN subscription_plans.api_access_enabled IS '是否支持API访问';
COMMENT ON COLUMN subscription_plans.level IS '套餐级别：FREE, BASIC, PRO';

-- 插入默认套餐数据
INSERT INTO subscription_plans (name, description, price, duration_days, search_limit, export_limit, board_limit, ai_analysis_enabled, api_access_enabled, level) VALUES
('免费版', '适合个人用户尝鲜体验', 0.00, 30, 10, 5, 1, true, false, 'FREE'),
('基础版', '适合小型电商卖家', 29.99, 30, 100, 50, 5, true, false, 'BASIC'),
('专业版', '适合中型电商团队', 99.99, 30, -1, -1, -1, true, true, 'PRO');

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_subscription_plans_level ON subscription_plans(level);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_price ON subscription_plans(price);
