START TRANSACTION;

-- =========================
-- 2. 插入 70 条秒杀订单
-- =========================
INSERT INTO t_seckill_order (
    order_id,
    user_id,
    seckill_activity_id,
    seckill_product_id,
    buy_quantity,
    seckill_unit_price,
    origin_unit_price,
    discount_amount,
    status,
    create_time,
    pay_time,
    cancel_time
)
SELECT
    nums.n AS order_id,
    ((nums.n - 1) % 10) + 1 AS user_id,
    1 AS seckill_activity_id,
    CASE
        WHEN ((nums.n - 1) % 3) = 0 THEN 1
        WHEN ((nums.n - 1) % 3) = 1 THEN 2
        ELSE 3
    END AS seckill_product_id,
    CASE
        WHEN ((nums.n - 1) % 3) = 0 THEN 1
        WHEN ((nums.n - 1) % 3) = 1 THEN 2
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
                WHEN ((nums.n - 1) % 3) = 0 THEN 1
                WHEN ((nums.n - 1) % 3) = 1 THEN 2
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
    TIMESTAMP('2026-05-01 10:00:00') + INTERVAL (nums.n - 1) MINUTE AS create_time,
    CASE
        WHEN nums.n % 4 = 1 THEN TIMESTAMP('2026-05-01 10:03:00') + INTERVAL (nums.n - 1) MINUTE
        ELSE NULL
    END AS pay_time,
    CASE
        WHEN nums.n % 4 IN (2, 3) THEN TIMESTAMP('2026-05-01 10:10:00') + INTERVAL (nums.n - 1) MINUTE
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
) nums;

COMMIT;