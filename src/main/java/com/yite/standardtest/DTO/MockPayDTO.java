package com.yite.standardtest.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MockPayDTO {
    private String orderNo;
    private BigDecimal amount;
}
