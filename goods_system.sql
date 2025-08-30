/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : goods_system

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 26/08/2025 11:41:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `last_login` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'lele', 'scrypt:32768:8:1$Sh7fpO5g0usdveeN$73fb14f3004958bfc85641af7fe6ea2de97f335cd1d0a1a12a1b3a6c3bedd04973f4ba48b98c1de2516ce8ca32288a61cc9cfb1567d9466286e01bfef4c61492', '2025-08-09 11:37:59');
INSERT INTO `admin` VALUES (2, 'admin', 'scrypt:32768:8:1$qMPOCdL2hjKHfcbW$f3a306956f807dd7887ac4609d2aec3c71eca58d9f86e6f95a57e38b7ed70df7b5af9f3a766a5602138cda8f72d18cbf768fb61e755e00b4987a64d3f26018e4', '2025-08-09 12:21:35');
INSERT INTO `admin` VALUES (3, 'ling', 'scrypt:32768:8:1$s1UcREb40Colw6li$9b9335bbe8b8c7390c92a9944fb72be176e57e6995744f78530067071423986c37683933b1715bda4fa17d6d7cc2469f679a71022ee128742a6e043d71a45e54', NULL);

-- ----------------------------
-- Table structure for outbound_record
-- ----------------------------
DROP TABLE IF EXISTS `outbound_record`;
CREATE TABLE `outbound_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` LONGTEXT COMMENT '商品图片URL或base64数据',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `out_date` date NOT NULL COMMENT '出库日期',
  `quantity` int NOT NULL COMMENT '出库数量',
  `name` varchar(100) NOT NULL COMMENT '姓名' ,
  `payment_status` tinyint NOT NULL DEFAULT 0 COMMENT '付款状态：0-未付款，1-已付款' ,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_out_date`(`out_date`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `outbound_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of outbound_record
-- ----------------------------

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `image_url` LONGTEXT COMMENT '商品图片URL或base64数据',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位',
  `initial_stock` int NOT NULL DEFAULT 0 COMMENT '初始库存数量',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '金额/元',
  `remaining_quantity` int NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, NULL, '苹果', '红富士/中号', '斤', 100, 8.50, 850.00, 100, '2025-08-26 11:21:08', '2025-08-26 11:21:08');
INSERT INTO `product` VALUES (2, NULL, '香蕉', '进口/大号', '斤', 50, 6.80, 340.00, 50, '2025-08-26 11:21:08', '2025-08-26 11:21:08');
INSERT INTO `product` VALUES (3, NULL, '橘子', '本地/小号', '斤', 80, 4.20, 336.00, 80, '2025-08-26 11:21:08', '2025-08-26 11:21:08');

SET FOREIGN_KEY_CHECKS = 1;
