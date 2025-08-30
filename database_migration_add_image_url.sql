-- 数据库迁移脚本：为product表添加缺失的字段
USE goods_system;

-- 添加amount字段（如果不存在）
ALTER TABLE product ADD COLUMN IF NOT EXISTS amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额';

-- 添加remaining_quantity字段（如果不存在）
ALTER TABLE product ADD COLUMN IF NOT EXISTS remaining_quantity INT NOT NULL DEFAULT 0 COMMENT '剩余数量';

-- 添加image_url字段（如果不存在）
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(2000) COMMENT '商品图片URL';

-- 更新现有数据的amount字段（金额 = 初始库存 × 单价）
UPDATE product SET amount = initial_stock * price WHERE amount = 0;

-- 更新现有数据的remaining_quantity字段（初始值等于初始库存）
UPDATE product SET remaining_quantity = initial_stock WHERE remaining_quantity = 0;

-- 显示更新后的表结构
DESCRIBE product; 