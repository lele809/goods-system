-- 快速修复数据库表结构和数据
USE goods_system;

-- 1. 备份现有数据（如果有新数据的话）
CREATE TEMPORARY TABLE temp_product_backup AS 
SELECT * FROM product WHERE id > 3;

-- 2. 删除现有的product表
DROP TABLE IF EXISTS product;

-- 3. 重新创建product表（image_url字段放在最后，避免数据错位）
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位',
  `initial_stock` int NOT NULL DEFAULT 0 COMMENT '初始库存数量',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '金额/元',
  `remaining_quantity` int NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `image_url` VARCHAR(2000) COMMENT '商品图片URL',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- 4. 插入正确的测试数据
INSERT INTO `product` (id, name, spec, unit, initial_stock, price, amount, remaining_quantity, created_at, updated_at, image_url) 
VALUES 
(1, '苹果', '红富士/中号', '斤', 100, 8.50, 850.00, 100, '2025-08-26 11:21:08', '2025-08-26 11:21:08', NULL),
(2, '香蕉', '进口/大号', '斤', 50, 6.80, 340.00, 50, '2025-08-26 11:21:08', '2025-08-26 11:21:08', NULL),
(3, '橘子', '本地/小号', '斤', 80, 4.20, 336.00, 80, '2025-08-26 11:21:08', '2025-08-26 11:21:08', NULL);

-- 5. 如果有备份数据，恢复它们（这里可能需要手动调整）
-- INSERT INTO product SELECT * FROM temp_product_backup;

-- 6. 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_product_backup;

-- 7. 验证修复结果
SELECT '=== 修复完成，查看表结构 ===' as status;
DESCRIBE product;

SELECT '=== 查看数据 ===' as status;
SELECT * FROM product; 