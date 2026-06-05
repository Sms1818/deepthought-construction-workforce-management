package com.example.demo.overtime.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OvertimeSettlementResponse {

    private Long workerId;
    private String month;
    private Double settledHours;
    private BigDecimal settledAmount;
    private String status;
}