package com.example.demo.attendance.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ClockOutRequest {
    @NotNull
    private Long workerId;
}
