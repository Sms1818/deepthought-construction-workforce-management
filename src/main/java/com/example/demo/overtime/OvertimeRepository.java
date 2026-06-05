package com.example.demo.overtime;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OvertimeRepository extends JpaRepository<OvertimeEntry, Long> {

    List<OvertimeEntry> findByWorkerIdAndOvertimeDateBetween(
            Long workerId,
            LocalDate from,
            LocalDate to);

    List<OvertimeEntry> findByWorkerIdAndOvertimeDateBetweenAndSettlementStatus(
            Long workerId,
            LocalDate from,
            LocalDate to,
            SettlementStatus settlementStatus);
}