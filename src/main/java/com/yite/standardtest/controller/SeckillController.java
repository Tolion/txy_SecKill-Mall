package com.yite.standardtest.controller;

import com.yite.standardtest.DTO.SeckillProductDTO;
import com.yite.standardtest.DTO.SeckillProductInfoDTO;
import com.yite.standardtest.annotation.TokenBucketRateLimit;
import com.yite.standardtest.common.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yite.standardtest.service.SeckillService;
import com.yite.standardtest.VO.SeckillProductVO;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * Step1: 先完成秒杀接口契约，后续再接 Redis 原子扣减与下单逻辑
     * 约定业务码：
     * 200  - 抢购成功
     * 1001 - 库存不足
     * 1002 - 重复下单
     * 1003 - 活动未开始或已结束
     * 1004 - 系统繁忙，请重试
     */
    @PostMapping("/buy")
    @TokenBucketRateLimit(
            key = "seckill_buy",
            capacity = 12,
            refillTokensPerSecond = 4,
            requestedTokens = 1,
            message = "秒杀请求过于频繁，请稍后再试"
    )
    public ResponseResult<SeckillProductVO> seckill(@RequestBody SeckillProductDTO dto) {
        if (dto == null || dto.getSeckillProductId() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            return ResponseResult.error(1004, "请求参数不合法");
        }

        // TODO: Step2 开始接入 Redis 原子扣减与防重复逻辑
        SeckillProductVO vo = seckillService.seckill(dto);
        return ResponseResult.success(vo);
    }

    /**
     * 秒杀商品列表（参与秒杀：活动中 + redis 库存>0）
     */
    @GetMapping("/products")
    public ResponseResult<List<SeckillProductVO>> seckillProducts() {
        try {
            List<SeckillProductVO> list = seckillService.listSeckillProducts();
            return ResponseResult.success(list);
        } catch (RuntimeException e) {
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 读取 Redis 预热的秒杀详情（Hash）
     */
    @GetMapping("/products/{seckillProductId}/preheat-info")
    public ResponseResult<SeckillProductInfoDTO> preheatedInfo(@PathVariable Long seckillProductId) {
        try {
            SeckillProductInfoDTO dto = seckillService.getPreheatedSeckillProductInfo(seckillProductId);
            if (dto == null) {
                return ResponseResult.error(404, "预热详情不存在");
            }
            return ResponseResult.success(dto);
        } catch (RuntimeException e) {
            return ResponseResult.error(e.getMessage());
        }
    }
}