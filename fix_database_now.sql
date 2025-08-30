-- 立即修复数据库字段缺失问题
USE goods_system;

-- 添加image_url字段到product表
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(2000) COMMENT '商品图片URL';

-- 查看修复结果
DESCRIBE product; 