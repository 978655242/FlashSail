-- =====================================================
-- FlashSell 品类初始化数据
-- 版本: V2
-- 描述: 初始化45个固定品类数据
-- =====================================================

-- 插入四大类目组
INSERT INTO category_groups (id, name, sort_order) VALUES
(1, '工业用品', 1),
(2, '节日装饰', 2),
(3, '家居生活与百货', 3),
(4, '数码配件与小家电', 4);

-- 工业用品类目（约12个品类）
INSERT INTO categories (group_id, name, amazon_category_id) VALUES
(1, '五金工具', 'industrial-hardware'),
(1, '安全防护', 'safety-security'),
(1, '电气设备', 'electrical-equipment'),
(1, '测量仪器', 'measuring-instruments'),
(1, '焊接设备', 'welding-equipment'),
(1, '气动工具', 'pneumatic-tools'),
(1, '液压设备', 'hydraulic-equipment'),
(1, '工业照明', 'industrial-lighting'),
(1, '包装材料', 'packaging-materials'),
(1, '清洁设备', 'cleaning-equipment'),
(1, '搬运设备', 'material-handling'),
(1, '工业胶带', 'industrial-tape');

-- 节日装饰类目（约10个品类）
INSERT INTO categories (group_id, name, amazon_category_id) VALUES
(2, '圣诞装饰', 'christmas-decorations'),
(2, '万圣节装饰', 'halloween-decorations'),
(2, '复活节装饰', 'easter-decorations'),
(2, '情人节装饰', 'valentines-decorations'),
(2, '派对用品', 'party-supplies'),
(2, '婚庆用品', 'wedding-supplies'),
(2, '生日装饰', 'birthday-decorations'),
(2, '节日灯饰', 'holiday-lights'),
(2, '气球装饰', 'balloon-decorations'),
(2, '节日礼品包装', 'gift-wrapping');

-- 家居生活与百货类目（约13个品类）
INSERT INTO categories (group_id, name, amazon_category_id) VALUES
(3, '厨房用品', 'kitchen-supplies'),
(3, '浴室用品', 'bathroom-supplies'),
(3, '收纳整理', 'storage-organization'),
(3, '家纺布艺', 'home-textiles'),
(3, '家居装饰', 'home-decor'),
(3, '清洁用品', 'cleaning-supplies'),
(3, '宠物用品', 'pet-supplies'),
(3, '园艺用品', 'garden-supplies'),
(3, '户外用品', 'outdoor-supplies'),
(3, '汽车用品', 'automotive-supplies'),
(3, '办公用品', 'office-supplies'),
(3, '运动健身', 'sports-fitness'),
(3, '母婴用品', 'baby-supplies');

-- 数码配件与小家电类目（约10个品类）
INSERT INTO categories (group_id, name, amazon_category_id) VALUES
(4, '手机配件', 'phone-accessories'),
(4, '电脑配件', 'computer-accessories'),
(4, '音频设备', 'audio-equipment'),
(4, '智能穿戴', 'wearable-devices'),
(4, '充电设备', 'charging-equipment'),
(4, '数据线材', 'cables-connectors'),
(4, '小型家电', 'small-appliances'),
(4, '个护电器', 'personal-care-appliances'),
(4, '厨房电器', 'kitchen-appliances'),
(4, '智能家居', 'smart-home');
