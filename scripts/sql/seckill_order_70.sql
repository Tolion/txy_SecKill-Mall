-- 用途：严格参照 @t_order.txt 的普通订单样本，生成 70 条秒杀订单
-- 设计约束：
-- 1) 直接对照普通订单的 user_id / product_id / quantity / status / 时间字段风格
-- 2) seckill_activity_id 取 n，保证 `(user_id, seckill_activity_id, seckill_product_id)` 不重复
-- 3) seckill_product_id 映射到秒杀商品 1~3，确保和现有秒杀商品表一致
-- 4) 不使用 CTE，不使用临时表，兼容性更好

START TRANSACTION;

INSERT INTO `t_seckill_order`
(
    `order_id`,
    `user_id`,
    `seckill_activity_id`,
    `seckill_product_id`,
    `buy_quantity`,
    `seckill_unit_price`,
    `origin_unit_price`,
    `discount_amount`,
    `status`,
    `create_time`,
    `pay_time`,
    `cancel_time`
)
SELECT
    5000 + nums.n AS order_id,
    CASE
        WHEN nums.n <= 10 THEN ((nums.n - 1) % 10) + 1
        WHEN nums.n <= 20 THEN CASE nums.n
            WHEN 11 THEN 9 WHEN 12 THEN 8 WHEN 13 THEN 7 WHEN 14 THEN 6 WHEN 15 THEN 5
            WHEN 16 THEN 4 WHEN 17 THEN 3 WHEN 18 THEN 2 WHEN 19 THEN 1 ELSE 10 END
        ELSE ((nums.n - 1) % 10) + 1
    END AS user_id,
    1000 + nums.n AS seckill_activity_id,
    CASE
        WHEN ((nums.n - 1) % 3) = 0 THEN 1
        WHEN ((nums.n - 1) % 3) = 1 THEN 2
        ELSE 3
    END AS seckill_product_id,
    CASE
        WHEN ((nums.n - 1) % 4) = 0 THEN 1
        WHEN ((nums.n - 1) % 4) = 1 THEN 2
        WHEN ((nums.n - 1) % 4) = 2 THEN 3
        ELSE 1
    END AS buy_quantity,
    CASE
        WHEN ((nums.n - 1) % 3) = 0 THEN 15.90
        WHEN ((nums.n - 1) % 3) = 1 THEN 6.90
        ELSE 199.00
    END AS seckill_unit_price,
    CASE
        WHEN ((nums.n - 1) % 3) = 0 THEN 25.90
        WHEN ((nums.n - 1) % 3) = 1 THEN 12.50
        ELSE 299.00
    END AS origin_unit_price,
    ROUND(
        (
            CASE
                WHEN ((nums.n - 1) % 3) = 0 THEN 25.90 - 15.90
                WHEN ((nums.n - 1) % 3) = 1 THEN 12.50 - 6.90
                ELSE 299.00 - 199.00
            END
        ) * (
            CASE
                WHEN ((nums.n - 1) % 4) = 0 THEN 1
                WHEN ((nums.n - 1) % 4) = 1 THEN 2
                WHEN ((nums.n - 1) % 4) = 2 THEN 3
                ELSE 1
            END
        ),
        2
    ) AS discount_amount,
    CASE
        WHEN nums.n % 4 = 0 THEN 0
        WHEN nums.n % 4 = 1 THEN 1
        WHEN nums.n % 4 = 2 THEN 2
        ELSE 3
    END AS status,
    CASE
        WHEN nums.n <= 10 THEN TIMESTAMP('2026-05-01 09:00:00') + INTERVAL (nums.n - 1) MINUTE
        WHEN nums.n <= 20 THEN TIMESTAMP('2026-05-01 09:00:00') + INTERVAL (nums.n - 11) MINUTE
        WHEN nums.n <= 30 THEN TIMESTAMP('2026-05-01 09:20:00') + INTERVAL (nums.n - 21) MINUTE
        WHEN nums.n <= 40 THEN TIMESTAMP('2026-05-01 09:40:00') + INTERVAL (nums.n - 31) MINUTE
        WHEN nums.n <= 50 THEN TIMESTAMP('2026-05-01 10:00:00') + INTERVAL (nums.n - 41) MINUTE
        WHEN nums.n <= 60 THEN TIMESTAMP('2026-05-01 10:20:00') + INTERVAL (nums.n - 51) MINUTE
        ELSE TIMESTAMP('2026-05-01 10:40:00') + INTERVAL (nums.n - 61) MINUTE
    END AS create_time,
    CASE
        WHEN nums.n % 4 = 1 THEN
            CASE
                WHEN nums.n <= 10 THEN TIMESTAMP('2026-05-01 09:05:00') + INTERVAL (nums.n - 1) MINUTE
                WHEN nums.n <= 20 THEN TIMESTAMP('2026-05-01 09:05:00') + INTERVAL (nums.n - 11) MINUTE
                WHEN nums.n <= 30 THEN TIMESTAMP('2026-05-01 09:25:00') + INTERVAL (nums.n - 21) MINUTE
                WHEN nums.n <= 40 THEN TIMESTAMP('2026-05-01 09:45:00') + INTERVAL (nums.n - 31) MINUTE
                WHEN nums.n <= 50 THEN TIMESTAMP('2026-05-01 10:05:00') + INTERVAL (nums.n - 41) MINUTE
                WHEN nums.n <= 60 THEN TIMESTAMP('2026-05-01 10:25:00') + INTERVAL (nums.n - 51) MINUTE
                ELSE TIMESTAMP('2026-05-01 10:45:00') + INTERVAL (nums.n - 61) MINUTE
            END
        ELSE NULL
    END AS pay_time,
    CASE
        WHEN nums.n % 4 IN (2, 3) THEN
            CASE
                WHEN nums.n <= 10 THEN TIMESTAMP('2026-05-02 09:00:00') + INTERVAL (nums.n - 1) MINUTE
                WHEN nums.n <= 20 THEN TIMESTAMP('2026-05-02 09:00:00') + INTERVAL (nums.n - 11) MINUTE
                WHEN nums.n <= 30 THEN TIMESTAMP('2026-05-02 09:20:00') + INTERVAL (nums.n - 21) MINUTE
                WHEN nums.n <= 40 THEN TIMESTAMP('2026-05-02 09:40:00') + INTERVAL (nums.n - 31) MINUTE
                WHEN nums.n <= 50 THEN TIMESTAMP('2026-05-02 10:00:00') + INTERVAL (nums.n - 41) MINUTE
                WHEN nums.n <= 60 THEN TIMESTAMP('2026-05-02 10:20:00') + INTERVAL (nums.n - 51) MINUTE
                ELSE TIMESTAMP('2026-05-02 10:40:00') + INTERVAL (nums.n - 61) MINUTE
            END
        ELSE NULL
    END AS cancel_time
FROM (
    SELECT ones.n + tens.n * 10 AS n
    FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) ones
    CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) tens
    WHERE ones.n + tens.n * 10 BETWEEN 1 AND 70
) nums
ORDER BY nums.n;

COMMIT;
