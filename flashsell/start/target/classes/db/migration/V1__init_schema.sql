-- =====================================================
-- FlashSell 数据库初始化脚本
-- 版本: V1
-- 描述: 创建所有基础表结构
-- =====================================================

-- =====================================================
-- 用户相关表
-- =====================================================

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    email VARCHAR(255),
    subscription_level VARCHAR(20) DEFAULT 'FREE',
    subscription_expire_date DATE,
    notification_enabled BOOLEAN DEFAULT TRUE,
    email_subscribed BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(100),
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID，主键自增';
COMMENT ON COLUMN users.phone IS '手机号，唯一';
COMMENT ON COLUMN users.password_hash IS '密码哈希值（BCrypt加密）';
COMMENT ON COLUMN users.nickname IS '用户昵称';
COMMENT ON COLUMN users.avatar_url IS '头像URL';
COMMENT ON COLUMN users.email IS '邮箱地址';
COMMENT ON COLUMN users.subscription_level IS '订阅等级：FREE-免费版, BASIC-基础版, PRO-专业版';
COMMENT ON COLUMN users.subscription_expire_date IS '订阅到期日期';
COMMENT ON COLUMN users.notification_enabled IS '是否开启消息通知';
COMMENT ON COLUMN users.email_subscribed IS '是否订阅邮件';
COMMENT ON COLUMN users.two_factor_enabled IS '是否开启两步验证';
COMMENT ON COLUMN users.two_factor_secret IS 'TOTP两步验证密钥';
COMMENT ON COLUMN users.phone_verified IS '手机号是否已验证';
COMMENT ON COLUMN users.last_login_time IS '最后登录时间';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';
COMMENT ON COLUMN users.deleted_at IS '删除时间（软删除）';


-- 用户邀请表
CREATE TABLE user_invites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    invite_code VARCHAR(20) UNIQUE NOT NULL,
    invited_count INTEGER DEFAULT 0,
    reward_days INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_invites IS '用户邀请表';
COMMENT ON COLUMN user_invites.id IS '主键ID';
COMMENT ON COLUMN user_invites.user_id IS '用户ID，关联users表';
COMMENT ON COLUMN user_invites.invite_code IS '邀请码，唯一';
COMMENT ON COLUMN user_invites.invited_count IS '已邀请人数';
COMMENT ON COLUMN user_invites.reward_days IS '获得的奖励天数';
COMMENT ON COLUMN user_invites.created_at IS '创建时间';

-- 邀请记录表
CREATE TABLE invite_records (
    id BIGSERIAL PRIMARY KEY,
    inviter_id BIGINT REFERENCES users(id),
    invitee_id BIGINT REFERENCES users(id),
    reward_days INTEGER DEFAULT 7,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE invite_records IS '邀请记录表';
COMMENT ON COLUMN invite_records.id IS '主键ID';
COMMENT ON COLUMN invite_records.inviter_id IS '邀请人ID';
COMMENT ON COLUMN invite_records.invitee_id IS '被邀请人ID';
COMMENT ON COLUMN invite_records.reward_days IS '奖励天数';
COMMENT ON COLUMN invite_records.created_at IS '创建时间';

-- 用户使用统计表（按月）
CREATE TABLE user_usage_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    month VARCHAR(7) NOT NULL,
    search_count INTEGER DEFAULT 0,
    export_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, month)
);

COMMENT ON TABLE user_usage_stats IS '用户使用统计表（按月统计）';
COMMENT ON COLUMN user_usage_stats.id IS '主键ID';
COMMENT ON COLUMN user_usage_stats.user_id IS '用户ID';
COMMENT ON COLUMN user_usage_stats.month IS '统计月份，格式：YYYY-MM';
COMMENT ON COLUMN user_usage_stats.search_count IS '搜索次数';
COMMENT ON COLUMN user_usage_stats.export_count IS '导出次数';
COMMENT ON COLUMN user_usage_stats.created_at IS '创建时间';
COMMENT ON COLUMN user_usage_stats.updated_at IS '更新时间';

-- =====================================================
-- 品类相关表
-- =====================================================

-- 品类组表
CREATE TABLE category_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE category_groups IS '品类组表（四大类目）';
COMMENT ON COLUMN category_groups.id IS '主键ID';
COMMENT ON COLUMN category_groups.name IS '类目组名称：工业用品、节日装饰、家居生活与百货、数码配件与小家电';
COMMENT ON COLUMN category_groups.sort_order IS '排序序号';
COMMENT ON COLUMN category_groups.created_at IS '创建时间';

-- 品类表
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT REFERENCES category_groups(id),
    name VARCHAR(100) NOT NULL,
    amazon_category_id VARCHAR(50),
    product_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE categories IS '品类表（45个固定品类）';
COMMENT ON COLUMN categories.id IS '主键ID';
COMMENT ON COLUMN categories.group_id IS '所属类目组ID';
COMMENT ON COLUMN categories.name IS '品类名称';
COMMENT ON COLUMN categories.amazon_category_id IS 'Amazon品类ID';
COMMENT ON COLUMN categories.product_count IS '品类下产品数量';
COMMENT ON COLUMN categories.created_at IS '创建时间';


-- =====================================================
-- 产品相关表
-- =====================================================

-- 产品表
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    asin VARCHAR(20) UNIQUE,
    title VARCHAR(500) NOT NULL,
    image_url VARCHAR(500),
    current_price DECIMAL(10,2),
    bsr_rank INTEGER,
    review_count INTEGER DEFAULT 0,
    rating DECIMAL(2,1),
    category_id BIGINT REFERENCES categories(id),
    competition_score DECIMAL(3,2),
    ai_recommendation TEXT,
    last_updated TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE products IS '产品表';
COMMENT ON COLUMN products.id IS '主键ID';
COMMENT ON COLUMN products.asin IS 'Amazon标准识别号，唯一';
COMMENT ON COLUMN products.title IS '产品标题';
COMMENT ON COLUMN products.image_url IS '产品图片URL';
COMMENT ON COLUMN products.current_price IS '当前价格（美元）';
COMMENT ON COLUMN products.bsr_rank IS 'BSR排名（Best Sellers Rank）';
COMMENT ON COLUMN products.review_count IS '评论数量';
COMMENT ON COLUMN products.rating IS '评分（1.0-5.0）';
COMMENT ON COLUMN products.category_id IS '所属品类ID';
COMMENT ON COLUMN products.competition_score IS '竞争评分（0.00-1.00）';
COMMENT ON COLUMN products.ai_recommendation IS 'AI推荐理由';
COMMENT ON COLUMN products.last_updated IS '数据最后更新时间';
COMMENT ON COLUMN products.created_at IS '创建时间';

-- 产品价格历史表
CREATE TABLE product_price_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    price DECIMAL(10,2) NOT NULL,
    recorded_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, recorded_date)
);

COMMENT ON TABLE product_price_history IS '产品价格历史表';
COMMENT ON COLUMN product_price_history.id IS '主键ID';
COMMENT ON COLUMN product_price_history.product_id IS '产品ID';
COMMENT ON COLUMN product_price_history.price IS '价格（美元）';
COMMENT ON COLUMN product_price_history.recorded_date IS '记录日期';
COMMENT ON COLUMN product_price_history.created_at IS '创建时间';

-- =====================================================
-- 收藏与看板相关表
-- =====================================================

-- 用户收藏表
CREATE TABLE user_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    product_id BIGINT REFERENCES products(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

COMMENT ON TABLE user_favorites IS '用户收藏表';
COMMENT ON COLUMN user_favorites.id IS '主键ID';
COMMENT ON COLUMN user_favorites.user_id IS '用户ID';
COMMENT ON COLUMN user_favorites.product_id IS '产品ID';
COMMENT ON COLUMN user_favorites.created_at IS '收藏时间';

-- 看板表
CREATE TABLE boards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE boards IS '看板表';
COMMENT ON COLUMN boards.id IS '主键ID';
COMMENT ON COLUMN boards.user_id IS '用户ID';
COMMENT ON COLUMN boards.name IS '看板名称';
COMMENT ON COLUMN boards.created_at IS '创建时间';
COMMENT ON COLUMN boards.deleted_at IS '删除时间（软删除）';

-- 看板产品关联表
CREATE TABLE board_products (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT REFERENCES boards(id),
    product_id BIGINT REFERENCES products(id),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(board_id, product_id)
);

COMMENT ON TABLE board_products IS '看板产品关联表';
COMMENT ON COLUMN board_products.id IS '主键ID';
COMMENT ON COLUMN board_products.board_id IS '看板ID';
COMMENT ON COLUMN board_products.product_id IS '产品ID';
COMMENT ON COLUMN board_products.added_at IS '添加时间';


-- =====================================================
-- 订阅与支付相关表
-- =====================================================

-- 订阅订单表
CREATE TABLE subscription_orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    order_no VARCHAR(50) UNIQUE NOT NULL,
    plan_id BIGINT NOT NULL,
    period VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    alipay_trade_no VARCHAR(100),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE subscription_orders IS '订阅订单表';
COMMENT ON COLUMN subscription_orders.id IS '主键ID';
COMMENT ON COLUMN subscription_orders.user_id IS '用户ID';
COMMENT ON COLUMN subscription_orders.order_no IS '订单号，唯一';
COMMENT ON COLUMN subscription_orders.plan_id IS '套餐ID';
COMMENT ON COLUMN subscription_orders.period IS '订阅周期：MONTHLY-月付, YEARLY-年付';
COMMENT ON COLUMN subscription_orders.amount IS '订单金额（人民币）';
COMMENT ON COLUMN subscription_orders.status IS '订单状态：PENDING-待支付, PAID-已支付, FAILED-失败, CANCELLED-已取消';
COMMENT ON COLUMN subscription_orders.alipay_trade_no IS '支付宝交易号';
COMMENT ON COLUMN subscription_orders.paid_at IS '支付时间';
COMMENT ON COLUMN subscription_orders.created_at IS '创建时间';

-- =====================================================
-- 爆品推荐相关表
-- =====================================================

-- 爆品推荐表
CREATE TABLE hot_products (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id),
    category_id BIGINT REFERENCES categories(id),
    hot_score DECIMAL(5,2) NOT NULL,
    rank_in_category INTEGER NOT NULL,
    recommend_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, recommend_date)
);

COMMENT ON TABLE hot_products IS '爆品推荐表（每日更新）';
COMMENT ON COLUMN hot_products.id IS '主键ID';
COMMENT ON COLUMN hot_products.product_id IS '产品ID';
COMMENT ON COLUMN hot_products.category_id IS '品类ID';
COMMENT ON COLUMN hot_products.hot_score IS '爆品评分（0-100）';
COMMENT ON COLUMN hot_products.rank_in_category IS '品类内排名';
COMMENT ON COLUMN hot_products.recommend_date IS '推荐日期';
COMMENT ON COLUMN hot_products.created_at IS '创建时间';

-- =====================================================
-- 历史记录相关表
-- =====================================================

-- 搜索历史表
CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    query TEXT NOT NULL,
    result_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE search_history IS '搜索历史表';
COMMENT ON COLUMN search_history.id IS '主键ID';
COMMENT ON COLUMN search_history.user_id IS '用户ID';
COMMENT ON COLUMN search_history.query IS '搜索查询内容';
COMMENT ON COLUMN search_history.result_count IS '搜索结果数量';
COMMENT ON COLUMN search_history.created_at IS '搜索时间';

-- 浏览历史表
CREATE TABLE browse_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    product_id BIGINT REFERENCES products(id),
    browsed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

COMMENT ON TABLE browse_history IS '浏览历史表';
COMMENT ON COLUMN browse_history.id IS '主键ID';
COMMENT ON COLUMN browse_history.user_id IS '用户ID';
COMMENT ON COLUMN browse_history.product_id IS '产品ID';
COMMENT ON COLUMN browse_history.browsed_at IS '浏览时间（重复浏览更新此时间）';

-- 热门关键词统计表
CREATE TABLE hot_keywords (
    id BIGSERIAL PRIMARY KEY,
    keyword VARCHAR(200) NOT NULL,
    search_count INTEGER DEFAULT 0,
    trend VARCHAR(20) DEFAULT 'STABLE',
    stat_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(keyword, stat_date)
);

COMMENT ON TABLE hot_keywords IS '热门关键词统计表（每日统计）';
COMMENT ON COLUMN hot_keywords.id IS '主键ID';
COMMENT ON COLUMN hot_keywords.keyword IS '关键词';
COMMENT ON COLUMN hot_keywords.search_count IS '搜索次数';
COMMENT ON COLUMN hot_keywords.trend IS '趋势：UP-上升, DOWN-下降, STABLE-稳定';
COMMENT ON COLUMN hot_keywords.stat_date IS '统计日期';
COMMENT ON COLUMN hot_keywords.created_at IS '创建时间';


-- =====================================================
-- 索引
-- =====================================================
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_bsr ON products(bsr_rank);
CREATE INDEX idx_user_favorites_user ON user_favorites(user_id);
CREATE INDEX idx_boards_user ON boards(user_id);
CREATE INDEX idx_hot_products_date ON hot_products(recommend_date);
CREATE INDEX idx_hot_products_category_date ON hot_products(category_id, recommend_date);
CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_created ON search_history(user_id, created_at DESC);
CREATE INDEX idx_browse_history_user ON browse_history(user_id);
CREATE INDEX idx_browse_history_browsed ON browse_history(user_id, browsed_at DESC);
CREATE INDEX idx_hot_keywords_date ON hot_keywords(stat_date);
