package com.example.demo.overtime;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.overtime.dto.OvertimeDateBreakdown;
import com.example.demo.overtime.dto.OvertimeSummaryResponse;
import com.example.demo.worker.Worker;
import com.example.demo.worker.WorkerRepository;

import java.math.BigDecimal;
import java.time.YearMonth;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.overtime.dto.OvertimeSettlementResponse;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OvertimeService {
    private final OvertimeRepository overtimeRepository;
    private final WorkerRepository workerRepository;

    public OvertimeSummaryResponse getMonthlySummary(
            Long workerId,
            LocalDate from,
            LocalDate to) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        List<OvertimeEntry> entries = overtimeRepository.findByWorkerIdAndOvertimeDateBetween(workerId, from, to);

        List<OvertimeDateBreakdown> breakdown = entries.stream()
                .map(entry -> OvertimeDateBreakdown.builder()
                        .date(entry.getOvertimeDate())
                        .overtimeHours(entry.getOvertimeHours())
                        .amount(entry.getAmount())
                        .settlementStatus(entry.getSettlementStatus().name())
                        .build())
                .toList();

        double totalHours = entries.stream()
                .mapToDouble(OvertimeEntry::getOvertimeHours)
                .sum();

        BigDecimal totalAmount = entries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OvertimeSummaryResponse.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .month(from.getYear() + "-" + String.format("%02d", from.getMonthValue()))
                .totalOvertimeHours(totalHours)
                .totalPayoutAmount(totalAmount)
                .breakdown(breakdown)
                .build();

    }

    @Transactional
    public OvertimeSettlementResponse settleOvertime(Long workerId, YearMonth month) {
        YearMonth currentMonth = YearMonth.now();
        if (!month.isBefore(currentMonth)) {
            throw new BadRequestException("Cannot settle current or future month");
        }

        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        List<OvertimeEntry> pendingEntries = overtimeRepository
                .findByWorkerIdAndOvertimeDateBetweenAndSettlementStatus(workerId, from, to, SettlementStatus.PENDING);

        if (pendingEntries.isEmpty()) {
            throw new ConflictException("No pending overtime entries found for settlement");
        }

        double settledHours = pendingEntries.stream()
                .mapToDouble(OvertimeEntry::getOvertimeHours)
                .sum();

        BigDecimal settledAmount = pendingEntries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pendingEntries.forEach(entry -> {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
        });

        overtimeRepository.saveAll(pendingEntries);

        return OvertimeSettlementResponse.builder()
                .workerId(workerId)
                .month(month.toString())
                .settledHours(settledHours)
                .settledAmount(settledAmount)
                .status(SettlementStatus.SETTLED.name())
                .build();
    }
}
