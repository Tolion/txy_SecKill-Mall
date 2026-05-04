-- 创建数据库
CREATE DATABASE IF NOT EXISTS `txy_miaosha`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `txy_miaosha`;

-- 创建用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- 所有用户的密码都是 "123456"
-- BCrypt 哈希值: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
INSERT INTO `t_user` (`username`, `password`, `phone`, `status`, `create_time`) VALUES
('zhangsan', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13800138001', 1, '2024-01-15 10:30:00'),
('lisi', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13900139001', 1, '2024-01-16 14:20:15'),
('wangwu', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13600136001', 0, '2024-01-17 09:45:30'),
('zhaoliu', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13700137001', 1, '2024-01-18 16:10:45'),
('sunqi', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13500135001', 1, '2024-01-19 11:25:00'),
('zhouba', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13400134001', 0, '2024-01-20 13:40:15'),
('wujiu', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13300133001', 1, '2024-01-21 15:55:30'),
('zhengshi', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13200132001', 1, '2024-01-22 08:10:45'),
('linshiyi', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13100131001', 1, '2024-01-23 17:25:00'),
('chenshier', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '13000130001', 1, '2024-01-24 12:40:15');





-- 重建普通商品表（先删后建）
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '商品名',
    `price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `img` VARCHAR(255) COMMENT '商品图片',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通商品表';

INSERT INTO `t_product` (`name`, `price`, `img`, `status`) VALUES
('新鲜草莓', 25.90, '/upload/productImages/productId_1_草莓.jpg', 1),
('美味糖果', 12.50, '/upload/productImages/productId_2_糖果.jpg', 1),
('无线耳机', 299.00, '/upload/productImages/productId_3_耳机.jpg', 1),
('蓝牙音箱', 189.00, '/upload/productImages/productId_4_音箱.jpg', 1),
('高清摄像头', 159.00, '/upload/productImages/productId_5_摄像头.jpg', 1),
('柔韧卷纸', 9.90, '/upload/productImages/productId_6_卷纸.jpg', 1),
('纯棉毛巾', 15.80, '/upload/productImages/productId_7_毛巾.jpg', 1),
('美妆蛋套装', 22.00, '/upload/productImages/productId_8_美妆蛋.jpg', 1),
('口红', 89.00, '/upload/productImages/productId_9_口红.jpg', 1),
('高性能显卡', 3299.00, '/upload/productImages/productId_10_显卡.jpg', 1);





-- 重建秒杀商品表（先删后建，兼容现有业务代码）
DROP TABLE IF EXISTS `t_seckill_product`;
CREATE TABLE `t_seckill_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '关联 t_product.id（兼容现有代码）',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `seckill_stock` INT NOT NULL DEFAULT 88888 COMMENT '秒杀库存总数',
    `seckill_remain_stock` INT NOT NULL DEFAULT 88888 COMMENT '剩余秒杀库存（热点更新）',
    `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
    `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
    `per_user_limit` INT NOT NULL DEFAULT 1 COMMENT '单用户限购数量',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '秒杀活动状态（0-未开始，1-进行中，2-已结束）',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防超卖）',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

INSERT INTO `t_seckill_product`
(`product_id`, `seckill_price`, `seckill_stock`, `seckill_remain_stock`, `start_time`, `end_time`, `per_user_limit`, `status`, `version`)
VALUES
-- 商品1：新鲜草莓（秒杀价 15.90）
(1, 15.90, 88888, 88888, '2026-05-10 09:00:00', '2026-05-10 23:59:59', 100, 0, 0),
-- 商品2：美味糖果（秒杀价 6.90）
(2, 6.90, 88888, 88888, '2026-05-11 10:00:00', '2026-05-11 22:00:00', 100, 1, 0),
-- 商品3：无线耳机（秒杀价 199.00）
(3, 199.00, 88888, 88888, '2026-05-12 00:00:00', '2026-05-12 23:59:59', 100, 2, 0);










-- 创建普通订单表，需要刻意冗余，应对商品下架或迭代
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
    `id` BIGINT PRIMARY KEY COMMENT '订单ID，采用雪花算法',
    `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) COMMENT '商品名称，应对商品下架或迭代',
    `product_img` VARCHAR(255) COMMENT '商品图片，应对商品下架或迭代',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '订单总价',
    `status` TINYINT NOT NULL COMMENT '订单状态：0-待支付 1-已支付 2-取消 3-超时关闭',
    `pay_amount` DECIMAL(10,2) COMMENT '实付金额',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    `pay_time` TIMESTAMP NULL COMMENT '订单支付时间',
    `expire_time` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 24 HOUR) COMMENT '订单自动关闭时间（默认24小时）',
    `trade_no` VARCHAR(64) UNIQUE COMMENT '第三方支付流水号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

INSERT INTO `t_order` (`id`, `order_no`, `user_id`, `product_id`, `product_name`, `product_img`, `quantity`, `total_price`, `status`, `pay_amount`, `create_time`, `pay_time`, `expire_time`, `trade_no`) VALUES
(1705744215123456789, 'ORD202401150001', 1, 1, '新鲜草莓', '/upload/productImages/productId_1_草莓.jpg', 2, 51.80, 1, 51.80, '2024-01-20 10:30:15', '2024-01-20 10:35:22', '2024-01-20 11:00:15', NULL),
(1705744215123456790, 'ORD202401160001', 2, 3, '无线耳机', '/upload/productImages/productId_3_耳机.jpg', 1, 299.00, 1, 299.00, '2024-01-21 14:20:30', '2024-01-21 14:25:10', '2024-01-21 14:50:30', NULL),
(1705744215123456791, 'ORD202401170001', 1, 6, '柔韧卷纸', '/upload/productImages/productId_6_卷纸.jpg', 5, 49.50, 2, NULL, '2024-01-22 09:45:18', NULL, '2024-01-22 10:15:18', NULL),
(1705744215123456792, 'SK202401180001', 4, 2, '美味糖果', '/upload/productImages/productId_2_糖果.jpg', 10, 125.00, 1, 125.00, '2024-01-23 20:00:05', '2024-01-23 20:00:30', '2024-01-23 20:30:05', NULL),
(1705744215123456793, 'ORD202401190001', 5, 4, '蓝牙音箱', '/upload/productImages/productId_4_音箱.jpg', 1, 189.00, 0, NULL, '2024-01-24 11:25:33', NULL, '2024-01-24 11:55:33', NULL),
(1705744215123456794, 'ORD202401200001', 2, 7, '纯棉毛巾', '/upload/productImages/productId_7_毛巾.jpg', 3, 47.40, 1, 47.40, '2024-01-25 13:40:28', '2024-01-25 13:45:15', '2024-01-25 14:10:28', NULL),
(1705744215123456795, 'SK202401210001', 7, 3, '无线耳机', '/upload/productImages/productId_3_耳机.jpg', 2, 398.00, 3, NULL, '2024-01-26 20:00:01', NULL, '2024-01-26 20:30:01', NULL),
(1705744215123456796, 'ORD202401220001', 8, 8, '美妆蛋套装', '/upload/productImages/productId_8_美妆蛋.jpg', 1, 22.00, 1, 22.00, '2024-01-27 08:10:45', '2024-01-27 08:15:20', '2024-01-27 08:40:45', NULL),
(1705744215123456797, 'ORD202401230001', 9, 9, '口红', '/upload/productImages/productId_9_口红.jpg', 2, 178.00, 0, NULL, '2024-01-28 17:25:10', NULL, '2024-01-28 17:55:10', NULL),
(1705744215123456798, 'ORD202401240001', 10, 10, '高性能显卡', '/upload/productImages/productId_10_显卡.jpg', 1, 3299.00, 1, 3299.00, '2024-01-29 12:40:15', '2024-01-29 12:45:30', '2024-01-29 13:10:15', NULL);





-- 重建秒杀订单表（先删后建）
-- 职责：仅存「秒杀域」快照与幂等维度；应付、履约、超时等以 t_order + 本条扩展为准。
DROP TABLE IF EXISTS `t_seckill_order_0`;
CREATE TABLE `t_seckill_order_0` (
    `id` BIGINT PRIMARY KEY COMMENT '秒杀订单ID，采用雪花算法',
    `sec_order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '秒杀订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `seckill_activity_id` BIGINT NOT NULL DEFAULT 1 COMMENT '秒杀活动ID（多活动时使用；单活动可先固定为1）',
    `seckill_product_id` BIGINT NOT NULL COMMENT '秒杀商品ID（关联 t_seckill_product.id）',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '下单时购买数量',
    `seckill_unit_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀单价快照（下单时锁定）',
    `origin_unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价快照（便于对账展示，可选）',
    `discount_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '单笔优惠快照（可按 origin - seckill 等方式填，可选）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2取消 3超时关闭 4退款中 5已退款',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` TIMESTAMP NULL COMMENT '支付时间',
    `cancel_time` TIMESTAMP NULL COMMENT '取消/关闭时间',
    `expire_time` TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 30 MINUTE) COMMENT '订单自动关闭时间（默认30分钟）',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单扩展表';

 INSERT INTO `t_seckill_order_0` (`id`, `sec_order_no`, `user_id`, `seckill_activity_id`, `seckill_product_id`, `quantity`, `seckill_unit_price`, `origin_unit_price`, `discount_amount`, `status`, `create_time`, `pay_time`, `cancel_time`, `expire_time`) VALUES
 -- 用户 zhaoliu(4)：秒杀糖果（t_seckill_product.id=2，原价取自 t_product 12.50）
(1234567890123456789, 'SEC20240123200005123', 4, 1, 2, 10, 6.90, 12.50, 5.60, 1, '2024-01-23 20:00:05', '2024-01-23 20:00:30', NULL, DEFAULT),
-- 用户 wujiu(7)：秒杀耳机（仅示例库存在 id=3 对应商品）
(1234567890123456790, 'SEC20240126200001123', 7, 1, 3, 2, 199.00, 299.00, 100.00, 3, '2024-01-26 20:00:01', NULL, NULL, DEFAULT);


-- （分表！）创建秒杀订单表
DROP TABLE IF EXISTS `t_seckill_order_1`;
CREATE TABLE t_seckill_order_1 LIKE t_seckill_order_0;
DROP TABLE IF EXISTS `t_seckill_order_2`;
CREATE TABLE t_seckill_order_2 LIKE t_seckill_order_0;
DROP TABLE IF EXISTS `t_seckill_order_3`;
CREATE TABLE t_seckill_order_3 LIKE t_seckill_order_0;