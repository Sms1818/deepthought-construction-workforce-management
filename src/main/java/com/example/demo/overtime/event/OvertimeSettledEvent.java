package com.example.demo.overtime.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OvertimeSettledEvent {

    private Long workerId;
    private String month;
    private Double settledHours;
    private BigDecimal settledAmount;
}