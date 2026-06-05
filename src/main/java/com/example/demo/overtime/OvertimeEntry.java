package com.example.demo.overtime;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.example.demo.attendance.AttendanceLog;
import com.example.demo.worker.Worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "overtime_entries", indexes = {
        @Index(name = "idx_overtime_worker_date", columnList = "worker_id,overtime_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "overtime_entry_sequence")
    @SequenceGenerator(name = "overtime_entry_sequence", sequenceName = "overtime_entry_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false, unique = true)
    private AttendanceLog attendance;

    @Column(name = "overtime_date", nullable = false)
    private LocalDate overtimeDate;

    @Column(nullable = false)
    private Double overtimeHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRateApplied;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;
}