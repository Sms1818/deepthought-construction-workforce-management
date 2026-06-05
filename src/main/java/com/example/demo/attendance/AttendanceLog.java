package com.example.demo.attendance;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.example.demo.site.Site;
import com.example.demo.worker.Worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_logs", indexes = {
        @Index(name = "idx_attendance_worker_clock_in", columnList = "worker_id, clock_in_time"),
        @Index(name = "idx_attendance_site_clock_in", columnList = "site_id, clock_in_time")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendance_log_sequence")
    @SequenceGenerator(name = "attendance_log_sequence", sequenceName = "attendance_log_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private Double totalHoursWorked;

    private Double overtimeHours;

    @Column(nullable = false)
    private Boolean flagged = false;

}
