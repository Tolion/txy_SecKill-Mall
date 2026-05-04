package com.yite.standardtest.preheat;

import com.yite.standardtest.DTO.SeckillProductInfoDTO;

public interface SeckillPreheatRedisService {
    SeckillProductInfoDTO getPreheatedInfo(Long seckillProductId);
}
