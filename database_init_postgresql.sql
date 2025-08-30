-- Supabase PostgreSQL 初始化脚本
-- 货架商品管理系统

-- 创建商品表 product
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    spec VARCHAR(100),
    unit VARCHAR(20) NOT NULL,
    initial_stock INTEGER NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    remaining_quantity INTEGER NOT NULL DEFAULT 0,
    image_url VARCHAR(2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_product_name ON product(name);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON product(created_at);

-- 创建出库记录表 outbound_record
CREATE TABLE IF NOT EXISTS outbound_record (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    out_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    name VARCHAR(50),
    payment_status INTEGER DEFAULT 0,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_outbound_product_id ON outbound_record(product_id);
CREATE INDEX IF NOT EXISTS idx_outbound_out_date ON outbound_record(out_date);
CREATE INDEX IF NOT EXISTS idx_outbound_created_at ON outbound_record(created_at);

-- 创建入库记录表 inbound_record
CREATE TABLE IF NOT EXISTS inbound_record (
    id BIGSERIAL PRIMARY KEY,
    image_url TEXT,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    in_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_inbound_product_id ON inbound_record(product_id);
CREATE INDEX IF NOT EXISTS idx_inbound_in_date ON inbound_record(in_date);
CREATE INDEX IF NOT EXISTS idx_inbound_created_at ON inbound_record(created_at);

-- 创建管理员表 admin
CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_admin_username ON admin(username);

-- 插入示例商品数据
INSERT INTO product (name, spec, unit, initial_stock, price, amount, remaining_quantity) VALUES
('苹果', '红富士/中号', '斤', 100, 8.50, 850.00, 100),
('香蕉', '进口/大号', '斤', 50, 6.80, 340.00, 50),
('橘子', '本地/小号', '斤', 80, 4.20, 336.00, 80)
ON CONFLICT DO NOTHING;

-- 插入默认管理员（用户名：admin，密码：admin123）
INSERT INTO admin (username, password_hash, salt) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXMerYCHw4B4HP.fQ0ql6O5.s8G', 'default_salt')
ON CONFLICT (username) DO NOTHING;

-- 显示创建的表
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';