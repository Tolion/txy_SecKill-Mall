package com.yite.standardtest.service.impl;

import com.yite.standardtest.DTO.SeckillProductDTO;
import com.yite.standardtest.DTO.SeckillMessageDTO;
import com.yite.standardtest.DTO.SeckillProductInfoDTO;
import com.yite.standardtest.VO.SeckillProductVO;
import com.yite.standardtest.common.security.context.LoginUser;
import com.yite.standardtest.common.security.context.LoginUserContext;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.entity.SeckillProductEntity;
import com.yite.standardtest.mapper.ProductMapper;
import com.yite.standardtest.mapper.SeckillProductMapper;
import com.yite.standardtest.service.SeckillService;
import com.yite.standardtest.MQ.SeckillOrderProducer;
import com.yite.standardtest.common.id.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class SeckillImpl implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SeckillOrderProducer seckillOrderProducer;

    @Autowired
    private com.yite.standardtest.preheat.SeckillPreheatRedisService seckillPreheatRedisService;

    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);
    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final long IDEMPOTENT_TTL_SECONDS = 3L;

    @Override
    @Transactional
    public SeckillProductVO seckill(SeckillProductDTO dto) {
        if (dto == null || dto.getSeckillProductId() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new RuntimeException("秒杀商品参数不合法");
        }

        Long seckillProductId = dto.getSeckillProductId();
        Integer quantity = dto.getQuantity();

        LoginUser user = LoginUserContext.get();
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("用户未登录");
        }
        Long userId = user.getUserId();

        SeckillProductEntity seckillProduct = seckillProductMapper.selectById(seckillProductId);
        if (seckillProduct == null) throw new RuntimeException("秒杀商品不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillProduct.getStartTime()) || now.isAfter(seckillProduct.getEndTime())) {
            throw new RuntimeException("秒杀活动未开始或已结束");
        }

        ProductEntity product = productMapper.selectById(seckillProduct.getProductId());
        if (product == null) throw new RuntimeException("商品不存在");

        int perUserLimit = seckillProduct.getPerUserLimit() == null || seckillProduct.getPerUserLimit() <= 0
                ? 1
                : seckillProduct.getPerUserLimit();
        if (quantity > perUserLimit) {
            throw new RuntimeException("超出限购数量，单用户最多可购买" + perUserLimit + "件");
        }

        // 短 TTL 幂等 key：只防止短时间内重复提交，不表示“已买过”
        String idemKey = "seckill:req:" + seckillProductId + ":" + userId;
        String idempotentLua =
                "local existed = redis.call('EXISTS', KEYS[1]); " +
                "if existed == 1 then return 0; end; " +
                "redis.call('SET', KEYS[1], '1', 'EX', ARGV[1]); " +
                "return 1;";
        RedisScript<Long> idempotentScript = new DefaultRedisScript<>(idempotentLua, Long.class);
        Long idempotentResult = stringRedisTemplate.execute(
                idempotentScript,
                Arrays.asList(idemKey),
                String.valueOf(IDEMPOTENT_TTL_SECONDS)
        );
        if (idempotentResult == null || idempotentResult == 0L) {
            throw new RuntimeException("请勿频繁购买，请稍后重试");
        }

        String stockKey = "seckill:stock:" + seckillProductId;
        String luaScript =
                "local stock = redis.call('get', KEYS[1]); " +
                "local qty = tonumber(ARGV[1]); " +
                "if stock and qty and tonumber(stock) >= qty then " +
                "  redis.call('decrby', KEYS[1], qty); " +
                "  return 1; " +
                "else return 0; end;";

        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        List<String> keys = Arrays.asList(stockKey);
        Long result = stringRedisTemplate.execute(redisScript, keys, quantity.toString());

        if (result == null || result == 0) {
            // 库存扣减失败时，清掉幂等 key，避免用户被短暂误伤
            stringRedisTemplate.delete(idemKey);
            throw new RuntimeException("库存不足");
        }

        String secOrderNo = "SEC" + now.format(ORDER_NO_TIME_FORMATTER) + userId + snowflakeIdGenerator.nextId();
        BigDecimal seckillUnitPrice = seckillProduct.getSeckillPrice();
        BigDecimal originUnitPrice = product.getPrice();
        BigDecimal discountAmount = originUnitPrice.subtract(seckillUnitPrice).multiply(BigDecimal.valueOf(quantity));
        LocalDateTime expireTime = now.plusHours(24);

        SeckillMessageDTO message = new SeckillMessageDTO();
        message.setMessageId(java.util.UUID.randomUUID().toString());
        message.setSecOrderNo(secOrderNo);
        message.setUserId(userId);
        message.setSeckillActivityId(1L);
        message.setSeckillProductId(seckillProductId);
        message.setVersion(0);
        message.setQuantity(quantity);
        message.setSeckillUnitPrice(seckillUnitPrice);
        message.setOriginUnitPrice(originUnitPrice);
        message.setDiscountAmount(discountAmount);
        message.setStatus(0);
        message.setCreateTime(now);
        message.setExpireTime(expireTime);

        try {
            seckillOrderProducer.sendSeckillOrder(message);
        } catch (Exception e) {
            try {
                String compensateLua =
                        "local qty = tonumber(ARGV[1]); " +
                        "redis.call('incrby', KEYS[1], qty); " +
                        "return 1;";
                RedisScript<Long> compensateScript = new DefaultRedisScript<>(compensateLua, Long.class);
                stringRedisTemplate.execute(compensateScript, keys, quantity.toString());
            } catch (Exception ex) {
                log.warn("秒杀补偿 Redis 失败 userId={} seckillProductId={}", userId, seckillProductId, ex);
            }

            // MQ 失败后，释放短 TTL 幂等 key，允许用户稍后重试
            stringRedisTemplate.delete(idemKey);
            log.warn("秒杀发送 MQ 失败，已做 Redis 补偿 userId={} seckillProductId={}", userId, seckillProductId, e);
            throw new RuntimeException("系统繁忙，请重试（MQ发送失败）");
        }

        SeckillProductVO vo = new SeckillProductVO();
        vo.setSeckillProductId(seckillProductId);
        vo.setSeckillProductName(product.getName());
        vo.setSeckillProductPrice(seckillProduct.getSeckillPrice());
        vo.setSeckillProductImg(product.getImg());
        vo.setSeckillProductQuantity(quantity);
        vo.setSeckillProductStartTime(seckillProduct.getStartTime());
        vo.setSeckillProductEndTime(seckillProduct.getEndTime());

        log.info("秒杀资格成功（已扣库存并投递 MQ） userId={} seckillProductId={} secOrderNo={}", userId, seckillProductId, secOrderNo);
        return vo;
    }

    @Override
    public List<SeckillProductVO> listSeckillProducts() {
        LocalDateTime now = LocalDateTime.now();

        List<SeckillProductEntity> allSeckillProducts = seckillProductMapper.selectList(null);

        return allSeckillProducts.stream()
                .filter(p -> p != null)
                .filter(p -> p.getStartTime() != null && p.getEndTime() != null)
                .filter(p -> !now.isBefore(p.getStartTime()) && !now.isAfter(p.getEndTime()))
                .sorted(Comparator.comparing(SeckillProductEntity::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> {
                    Long seckillProductId = p.getId();
                    String stockKey = "seckill:stock:" + seckillProductId;

                    String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
                    if (stockStr == null) return null;

                    int stock;
                    try {
                        stock = Integer.parseInt(stockStr);
                    } catch (Exception e) {
                        return null;
                    }
                    if (stock <= 0) return null;

                    ProductEntity product = productMapper.selectById(p.getProductId());
                    if (product == null) return null;

                    // 优先从预热 Hash 里读取摘要信息，避免前端卡片直接依赖数据库字段
                    SeckillProductInfoDTO info = getPreheatedSeckillProductInfo(seckillProductId);
                    String productName = info != null && info.getProductName() != null ? info.getProductName() : product.getName();
                    String productImg = info != null && info.getProductImg() != null ? info.getProductImg() : product.getImg();
                    BigDecimal seckillPrice = info != null && info.getSeckillPrice() != null ? info.getSeckillPrice() : p.getSeckillPrice();
                    LocalDateTime startTime = info != null && info.getStartTime() != null ? info.getStartTime() : p.getStartTime();
                    LocalDateTime endTime = info != null && info.getEndTime() != null ? info.getEndTime() : p.getEndTime();

                    SeckillProductVO vo = new SeckillProductVO();
                    vo.setSeckillProductId(seckillProductId);
                    vo.setSeckillProductName(productName);
                    vo.setSeckillProductPrice(seckillPrice);
                    vo.setSeckillProductImg(productImg);
                    vo.setSeckillProductQuantity(stock);
                    vo.setSeckillProductStartTime(startTime);
                    vo.setSeckillProductEndTime(endTime);
                    return vo;
                })
                .filter(vo -> vo != null)
                .toList();
    }

    @Override
    public SeckillProductInfoDTO getPreheatedSeckillProductInfo(Long seckillProductId) {
        return seckillPreheatRedisService.getPreheatedInfo(seckillProductId);
    }
}