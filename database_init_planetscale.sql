-- PlanetScale 专用初始化脚本
-- 注意：PlanetScale 不支持外键约束

-- 创建商品表 product
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    spec VARCHAR(100) COMMENT '规格',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    initial_stock INT NOT NULL DEFAULT 0 COMMENT '初始库存数量',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    remaining_quantity INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
    image_url VARCHAR(2000) COMMENT '商品图片URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 创建出库记录表 outbound_record
CREATE TABLE IF NOT EXISTS outbound_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    out_date DATE NOT NULL COMMENT '出库日期',
    quantity INT NOT NULL COMMENT '出库数量',
    name VARCHAR(50) COMMENT '姓名',
    payment_status INT DEFAULT 0 COMMENT '付款状态：0-未付款，1-已付款',
    image_url LONGTEXT COMMENT '商品图片URL或base64数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_id (product_id),
    INDEX idx_out_date (out_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库记录表';

-- 创建入库记录表 inbound_record
CREATE TABLE IF NOT EXISTS inbound_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    image_url LONGTEXT COMMENT '商品图片URL或base64数据',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    in_date DATE NOT NULL COMMENT '入库日期',
    quantity INT NOT NULL COMMENT '入库数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_id (product_id),
    INDEX idx_in_date (in_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库记录表';

-- 创建管理员表 admin
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 插入一些示例数据
INSERT INTO product (name, spec, unit, initial_stock, price, amount, remaining_quantity) VALUES
('苹果', '红富士/中号', '斤', 100, 8.50, 850.00, 100),
('香蕉', '进口/大号', '斤', 50, 6.80, 340.00, 50),
('橘子', '本地/小号', '斤', 80, 4.20, 336.00, 80);

-- 插入默认管理员（用户名：admin，密码：admin123）
INSERT INTO admin (username, password_hash, salt) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXMerYCHw4B4HP.fQ0ql6O5.s8G', 'default_salt');

-- 显示创建的表
SHOW TABLES;