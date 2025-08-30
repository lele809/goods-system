-- 简单修复方案：确保product表的image_url字段正确设置
-- 请在您的数据库管理工具中执行以下SQL语句

USE goods_system;

-- 检查当前表结构
DESCRIBE product;

-- 如果image_url字段不存在，添加它
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(2000) DEFAULT NULL COMMENT '商品图片URL' AFTER id;

-- 如果字段已存在但位置不对，可以重新调整位置
-- ALTER TABLE product MODIFY COLUMN image_url VARCHAR(2000) DEFAULT NULL COMMENT '商品图片URL' AFTER id;

-- 查看修复后的表结构
DESCRIBE product; 