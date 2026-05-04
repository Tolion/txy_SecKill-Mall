package com.yite.standardtest.service;

import com.yite.standardtest.DTO.SeckillProductDTO;
import com.yite.standardtest.DTO.SeckillProductInfoDTO;
import com.yite.standardtest.VO.SeckillProductVO;

import java.util.List;

public interface SeckillService{

    /**
     * 秒杀
     */
    SeckillProductVO seckill(SeckillProductDTO dto);

    /**
     * 展示秒杀商品列表（参与秒杀：活动中 + redis 库存>0）
     */
    List<SeckillProductVO> listSeckillProducts();

    /**
     * 读取 Redis 预热的秒杀商品详情（Hash）
     */
    SeckillProductInfoDTO getPreheatedSeckillProductInfo(Long seckillProductId);
}