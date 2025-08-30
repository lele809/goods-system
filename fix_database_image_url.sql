-- 修复product表的image_url字段问题
-- 如果表结构不匹配，重新创建表

USE goods_system;

-- 备份现有数据
CREATE TABLE IF NOT EXISTS product_backup AS SELECT * FROM product;

-- 删除原表
DROP TABLE IF EXISTS product;

-- 重新创建表（包含image_url字段）
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` VARCHAR(2000) DEFAULT NULL COMMENT '商品图片URL',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位',
  `initial_stock` int NOT NULL DEFAULT 0 COMMENT '初始库存数量',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '金额/元',
  `remaining_quantity` int NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- 从备份表恢复数据（为image_url设置NULL值）
INSERT INTO `product` (`id`, `image_url`, `name`, `spec`, `unit`, `initial_stock`, `price`, `amount`, `remaining_quantity`, `created_at`, `updated_at`)
SELECT `id`, NULL, `name`, `spec`, `unit`, `initial_stock`, `price`, `amount`, `remaining_quantity`, `created_at`, `updated_at`
FROM product_backup;

-- 删除备份表
DROP TABLE product_backup;

-- 重置自增ID
SELECT @max_id := MAX(id) FROM product;
SET @sql = CONCAT('ALTER TABLE product AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt; 