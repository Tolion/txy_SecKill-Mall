START TRANSACTION;

-- =========================
-- 1. 先生成 1~100 的数字源
-- =========================
INSERT INTO t_order (
    order_no,
    user_id,
    product_id,
    product_name,
    product_img,
    quantity,
    total_price,
    status,
    source,
    pay_amount,
    create_time,
    pay_time,
    expire_time,
    trade_no
)
SELECT
    CONCAT('ORD20260430', LPAD(nums.n, 4, '0')) AS order_no,
    ((nums.n - 1) % 10) + 1 AS user_id,
    ((nums.n - 1) % 10) + 1 AS product_id,
    CASE ((nums.n - 1) % 10)
        WHEN 0 THEN '新鲜草莓'
        WHEN 1 THEN '美味糖果'
        WHEN 2 THEN '无线耳机'
        WHEN 3 THEN '蓝牙音箱'
        WHEN 4 THEN '高清摄像头'
        WHEN 5 THEN '柔韧卷纸'
        WHEN 6 THEN '纯棉毛巾'
        WHEN 7 THEN '美妆蛋套装'
        WHEN 8 THEN '口红'
        ELSE '高性能显卡'
    END AS product_name,
    CASE ((nums.n - 1) % 10)
        WHEN 0 THEN '/upload/productImages/productId_1_草莓.jpg'
        WHEN 1 THEN '/upload/productImages/productId_2_糖果.jpg'
        WHEN 2 THEN '/upload/productImages/productId_3_耳机.jpg'
        WHEN 3 THEN '/upload/productImages/productId_4_音箱.jpg'
        WHEN 4 THEN '/upload/productImages/productId_5_摄像头.jpg'
        WHEN 5 THEN '/upload/productImages/productId_6_卷纸.jpg'
        WHEN 6 THEN '/upload/productImages/productId_7_毛巾.jpg'
        WHEN 7 THEN '/upload/productImages/productId_8_美妆蛋.jpg'
        WHEN 8 THEN '/upload/productImages/productId_9_口红.jpg'
        ELSE '/upload/productImages/productId_10_显卡.jpg'
    END AS product_img,
    ((nums.n - 1) % 5) + 1 AS quantity,
    ROUND(
        CASE ((nums.n - 1) % 10)
            WHEN 0 THEN 25.90
            WHEN 1 THEN 12.50
            WHEN 2 THEN 299.00
            WHEN 3 THEN 189.00
            WHEN 4 THEN 159.00
            WHEN 5 THEN 9.90
            WHEN 6 THEN 15.80
            WHEN 7 THEN 22.00
            WHEN 8 THEN 89.00
            ELSE 3299.00
        END * (((nums.n - 1) % 5) + 1), 2
    ) AS total_price,
    CASE
        WHEN nums.n % 4 = 0 THEN 0
        WHEN nums.n % 4 = 1 THEN 1
        WHEN nums.n % 4 = 2 THEN 2
        ELSE 3
    END AS status,
    1 AS source,
    CASE
        WHEN nums.n % 4 = 1 THEN ROUND(
            CASE ((nums.n - 1) % 10)
                WHEN 0 THEN 25.90
                WHEN 1 THEN 12.50
                WHEN 2 THEN 299.00
                WHEN 3 THEN 189.00
                WHEN 4 THEN 159.00
                WHEN 5 THEN 9.90
                WHEN 6 THEN 15.80
                WHEN 7 THEN 22.00
                WHEN 8 THEN 89.00
                ELSE 3299.00
            END * (((nums.n - 1) % 5) + 1), 2
        )
        ELSE NULL
    END AS pay_amount,
    TIMESTAMP('2026-05-01 09:00:00') + INTERVAL (nums.n - 1) MINUTE AS create_time,
    CASE
        WHEN nums.n % 4 = 1 THEN TIMESTAMP('2026-05-01 09:05:00') + INTERVAL (nums.n - 1) MINUTE
        ELSE NULL
    END AS pay_time,
    TIMESTAMP('2026-05-02 09:00:00') + INTERVAL (nums.n - 1) MINUTE AS expire_time,
    CASE
        WHEN 0 = 1 THEN CONCAT('TRADE', LPAD(nums.n, 8, '0'))
        ELSE NULL
    END AS trade_no
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
    WHERE ones.n + tens.n * 10 BETWEEN 1 AND 100
) nums;



COMMIT;