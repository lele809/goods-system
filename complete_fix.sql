-- 完整的数据库修复方案
-- 请在数据库管理工具中逐步执行以下SQL语句

USE goods_system;

-- 1. 首先检查当前表结构
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'goods_system' 
  AND TABLE_NAME = 'product'
ORDER BY ORDINAL_POSITION;

-- 2. 备份现有数据
DROP TABLE IF EXISTS product_backup_temp;
CREATE TABLE product_backup_temp AS 
SELECT 
    id,
    name,
    spec,
    unit,
    initial_stock,
    price,
    amount,
    remaining_quantity,
    created_at,
    updated_at
FROM product;

-- 3. 删除外键约束（如果存在）
SET FOREIGN_KEY_CHECKS = 0;

-- 4. 删除原表
DROP TABLE IF EXISTS product;

-- 5. 重新创建表（确保字段顺序正确）
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` varchar(2000) DEFAULT NULL COMMENT '商品图片URL',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `initial_stock` int NOT NULL DEFAULT 0 COMMENT '初始库存数量',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额/元',
  `remaining_quantity` int NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 6. 恢复数据（为image_url设置NULL）
INSERT INTO product (
    id, 
    image_url, 
    name, 
    spec, 
    unit, 
    initial_stock, 
    price, 
    amount, 
    remaining_quantity, 
    created_at, 
    updated_at
)
SELECT 
    id, 
    NULL as image_url, 
    name, 
    spec, 
    unit, 
    initial_stock, 
    price, 
    amount, 
    remaining_quantity, 
    created_at, 
    updated_at
FROM product_backup_temp;

-- 7. 重新创建outbound_record表的外键约束
ALTER TABLE outbound_record 
ADD CONSTRAINT `outbound_record_ibfk_1` 
FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) 
ON DELETE CASCADE ON UPDATE RESTRICT;

-- 8. 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 9. 重置自增ID
SELECT @max_id := COALESCE(MAX(id), 0) FROM product;
SET @sql = CONCAT('ALTER TABLE product AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 10. 删除临时备份表
DROP TABLE product_backup_temp;

-- 11. 验证表结构
DESCRIBE product;

-- 12. 查看数据
SELECT id, image_url, name, spec, unit FROM product LIMIT 5; 