package com.example.demo.overtime.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OvertimeSummaryResponse {

    private Long workerId;
    private String workerName;
    private String month;
    private Double totalOvertimeHours;
    private BigDecimal totalPayoutAmount;
    private List<OvertimeDateBreakdown> breakdown;
}