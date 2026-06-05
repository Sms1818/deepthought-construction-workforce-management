package com.example.demo.overtime;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.overtime.dto.OvertimeSettlementResponse;
import com.example.demo.overtime.dto.OvertimeSummaryResponse;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/overtime")
@AllArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @GetMapping("/summary/{workerId}")
    public OvertimeSummaryResponse getMonthlySummary(
            @PathVariable Long workerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return overtimeService.getMonthlySummary(
                workerId,
                month.atDay(1),
                month.plusMonths(1).atDay(1));
    }

    @PostMapping("/settle/{workerId}")
    public OvertimeSettlementResponse settleOvertime(
            @PathVariable Long workerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return overtimeService.settleOvertime(workerId, month);
    }

}