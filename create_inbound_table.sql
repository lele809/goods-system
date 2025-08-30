-- 创建入库记录表
CREATE TABLE IF NOT EXISTS `inbound_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` LONGTEXT COMMENT '商品图片URL或base64数据',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `in_date` date NOT NULL COMMENT '入库日期',
  `quantity` int NOT NULL COMMENT '入库数量',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_in_date`(`in_date`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `inbound_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库记录表' ROW_FORMAT = Dynamic;

-- 插入一些示例数据（可选）
-- INSERT INTO `inbound_record` (`product_id`, `in_date`, `quantity`) VALUES
-- (1, '2024-01-15', 50),
-- (2, '2024-01-16', 30),
-- (3, '2024-01-17', 20);