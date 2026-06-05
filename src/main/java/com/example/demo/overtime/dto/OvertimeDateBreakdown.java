package com.example.demo.overtime.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OvertimeDateBreakdown {

    private LocalDate date;
    private Double overtimeHours;
    private BigDecimal amount;
    private String settlementStatus;
}