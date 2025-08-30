-- 为现有的product表添加image_url字段
USE goods_system;

-- 添加image_url字段
ALTER TABLE product ADD COLUMN image_url VARCHAR(2000) COMMENT '商品图片URL';
 
-- 查看更新后的表结构
DESCRIBE product; 