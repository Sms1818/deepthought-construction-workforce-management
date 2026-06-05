package com.example.demo.attendance.dto;

import java.time.LocalDateTime;

import com.example.demo.attendance.AttendanceLog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponse {

    private Long attendanceId;

    private Long workerId;
    private String workerName;

    private Long siteId;
    private String siteName;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    private Double totalHoursWorked;
    private Double overtimeHours;
    private Boolean flagged;

    public static AttendanceResponse fromEntity(AttendanceLog log) {
        return AttendanceResponse.builder()
                .attendanceId(log.getId())
                .workerId(log.getWorker().getId())
                .workerName(log.getWorker().getName())
                .siteId(log.getSite().getId())
                .siteName(log.getSite().getSiteName())
                .clockInTime(log.getClockInTime())
                .clockOutTime(log.getClockOutTime())
                .totalHoursWorked(log.getTotalHoursWorked())
                .overtimeHours(log.getOvertimeHours())
                .flagged(log.getFlagged())
                .build();
    }
}