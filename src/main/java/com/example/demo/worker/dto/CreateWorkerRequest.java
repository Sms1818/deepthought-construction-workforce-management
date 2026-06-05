package com.example.demo.worker.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.example.demo.worker.Designation;

import lombok.Data;

@Data
public class CreateWorkerRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    private Designation designation;

    @NotNull
    @Positive
    private BigDecimal dailyWageRate;
}