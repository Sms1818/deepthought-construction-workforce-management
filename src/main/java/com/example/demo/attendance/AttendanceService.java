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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

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

    @Transactional
    public AttendanceLog clockIn(ClockInRequest request) {
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

        return savedAttendanceLog;

    }

    @Transactional
    public AttendanceLog clockOut(ClockOutRequest request) {
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
        activeWorkerCacheService.removeActiveWorker(request.getWorkerId());

        return savedAttendanceLog;
    }

    public List<ActiveWorkerCacheDto> getActiveWorkers() {
        return activeWorkerCacheService.getActiveWorkers();
    }

    public Page<AttendanceLog> getAttendanceLog(
            Long workerId,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {
        return attendanceRepository.findByWorkerIdAndClockInTimeBetween(
                workerId,
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(page, size));
    }

}
