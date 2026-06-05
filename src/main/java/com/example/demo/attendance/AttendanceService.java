package com.example.demo.attendance;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.attendance.dto.ActiveWorkerCacheDto;

import java.util.List;

import com.example.demo.attendance.dto.ClockInRequest;
import com.example.demo.attendance.dto.ClockOutRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.site.Site;
import com.example.demo.site.SiteRepository;
import com.example.demo.worker.Worker;
import com.example.demo.worker.WorkerRepository;

import com.example.demo.overtime.OvertimeEntry;
import com.example.demo.overtime.OvertimeRepository;
import com.example.demo.overtime.SettlementStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import com.example.demo.attendance.dto.AttendanceResponse;
import com.example.demo.overtime.OvertimeRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AttendanceService {
    private static final double STANDARD_SHIFT_HOURS = 8.0;
    private static final double MAX_SHIFT_HOURS = 16.0;

    private final AttendanceRepository attendanceRepository;
    private final SiteRepository siteRepository;
    private final WorkerRepository workerRepository;
    private final ActiveWorkerCacheService activeWorkerCacheService;
    private final OvertimeRepository overtimeRepository;

    @Transactional
    public AttendanceResponse clockIn(ClockInRequest request) {
        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        if (!Boolean.TRUE.equals(worker.getActive())) {
            throw new ConflictException("Worker is not active");
        }

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        if (!Boolean.TRUE.equals(site.getActive())) {
            throw new ConflictException("Site is not active");
        }

        attendanceRepository.findByWorkerIdAndClockOutTimeIsNull(worker.getId())
                .ifPresent(existing -> {
                    throw new ConflictException("Worker is already clocked in");
                });

        AttendanceLog attendanceLog = AttendanceLog.builder()
                .worker(worker)
                .site(site)
                .clockInTime(LocalDateTime.now())
                .flagged(false)
                .build();

        AttendanceLog savedAttendanceLog = attendanceRepository.save(attendanceLog);

        ActiveWorkerCacheDto cacheDto = ActiveWorkerCacheDto.builder()
                .workerId(worker.getId())
                .workerName(worker.getName())
                .siteId(site.getId())
                .siteName(site.getSiteName())
                .clockInTime(savedAttendanceLog.getClockInTime())
                .build();

        activeWorkerCacheService.addActiveWorker(cacheDto);

        return AttendanceResponse.fromEntity(savedAttendanceLog);

    }

    @Transactional
    public AttendanceResponse clockOut(ClockOutRequest request) {
        AttendanceLog attendanceLog = attendanceRepository
                .findByWorkerIdAndClockOutTimeIsNull(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker is not currently clocked in"));

        LocalDateTime clockOutTime = LocalDateTime.now();
        LocalDateTime clockInTime = attendanceLog.getClockInTime();

        double totalHours = Duration.between(clockInTime, clockOutTime).toMinutes() / 60.0;
        double overtimeHours = Math.max(0, totalHours - STANDARD_SHIFT_HOURS);

        attendanceLog.setClockOutTime(clockOutTime);
        attendanceLog.setTotalHoursWorked(totalHours);
        attendanceLog.setOvertimeHours(overtimeHours);
        attendanceLog.setFlagged(totalHours > MAX_SHIFT_HOURS);

        AttendanceLog savedAttendanceLog = attendanceRepository.save(attendanceLog);

        if (overtimeHours > 0) {
            double remainingMonthlyCap = getRemainingMonthlyOvertimeCap(
                    attendanceLog.getWorker().getId(),
                    clockOutTime.toLocalDate());

            double cappedOvertimeHours = Math.min(overtimeHours, remainingMonthlyCap);

            if (cappedOvertimeHours > 0) {
                BigDecimal amount = calculateOvertimeAmount(
                        attendanceLog.getWorker().getDailyWageRate(),
                        cappedOvertimeHours);

                OvertimeEntry overtimeEntry = OvertimeEntry.builder()
                        .worker(attendanceLog.getWorker())
                        .attendance(savedAttendanceLog)
                        .overtimeDate(clockOutTime.toLocalDate())
                        .overtimeHours(cappedOvertimeHours)
                        .overtimeRateApplied(BigDecimal.valueOf(1.5))
                        .amount(amount)
                        .settlementStatus(SettlementStatus.PENDING)
                        .build();

                overtimeRepository.save(overtimeEntry);
            }
        }

        activeWorkerCacheService.removeActiveWorker(request.getWorkerId());

        return AttendanceResponse.fromEntity(savedAttendanceLog);
    }

    public List<ActiveWorkerCacheDto> getActiveWorkers() {
        return activeWorkerCacheService.getActiveWorkers();
    }

    public Page<AttendanceResponse> getAttendanceLog(
            Long workerId,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {
        Page<AttendanceLog> logs = attendanceRepository.findByWorkerIdAndClockInTimeBetween(
                workerId,
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(page, size));

        return logs.map(AttendanceResponse::fromEntity);
    }

    private BigDecimal calculateOvertimeAmount(BigDecimal dailyWageRate, double overtimeHours) {
        BigDecimal hourlyRate = dailyWageRate.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);

        double firstTwoHours = Math.min(overtimeHours, 2.0);
        double remainingHours = Math.max(0, overtimeHours - 2.0);

        BigDecimal firstAmount = hourlyRate
                .multiply(BigDecimal.valueOf(1.5))
                .multiply(BigDecimal.valueOf(firstTwoHours));

        BigDecimal remainingAmount = hourlyRate
                .multiply(BigDecimal.valueOf(2.0))
                .multiply(BigDecimal.valueOf(remainingHours));

        return firstAmount.add(remainingAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private double getRemainingMonthlyOvertimeCap(Long workerId, LocalDate date) {
        LocalDate from = date.withDayOfMonth(1);
        LocalDate to = from.plusMonths(1);

        double alreadyRecordedHours = overtimeRepository
                .findByWorkerIdAndOvertimeDateBetween(workerId, from, to)
                .stream()
                .mapToDouble(OvertimeEntry::getOvertimeHours)
                .sum();

        return Math.max(0, 60.0 - alreadyRecordedHours);
    }

}
