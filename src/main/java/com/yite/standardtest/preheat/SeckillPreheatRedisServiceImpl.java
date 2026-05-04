package com.yite.standardtest.preheat;

import com.yite.standardtest.DTO.SeckillProductInfoDTO;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SeckillPreheatRedisServiceImpl implements SeckillPreheatRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public SeckillPreheatRedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public SeckillProductInfoDTO getPreheatedInfo(Long seckillProductId) {
        String infoKey = SeckillPreheatKeys.infoKey(seckillProductId);
        HashOperations<String, Object, Object> hashOps = stringRedisTemplate.opsForHash();
        Map<Object, Object> entries = hashOps.entries(infoKey);
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        SeckillProductInfoDTO dto = new SeckillProductInfoDTO();
        dto.setSeckillProductId(parseLong(entries.get("seckillProductId")));
        dto.setProductId(parseLong(entries.get("productId")));
        dto.setProductName(toString(entries.get("productName")));
        dto.setProductImg(toString(entries.get("productImg")));
        dto.setSeckillPrice(parseDecimal(entries.get("seckillPrice")));
        dto.setStock(parseInt(entries.get("stock")));
        dto.setRemainStock(parseInt(entries.get("remainStock")));
        dto.setPerUserLimit(parseInt(entries.get("perUserLimit")));
        dto.setStatus(parseInt(entries.get("status")));
        dto.setVersion(parseInt(entries.get("version")));
        dto.setStartTime(parseDateTime(entries.get("startTime")));
        dto.setEndTime(parseDateTime(entries.get("endTime")));
        dto.setPreheatTime(parseDateTime(entries.get("preheatTime")));
        return dto;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long parseLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return Long.parseLong(String.valueOf(value));
    }

    private Integer parseInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return LocalDateTime.parse(String.valueOf(value));
    }
}
