package com.example.demo.attendance;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.attendance.dto.ActiveWorkerCacheDto;
import com.example.demo.attendance.dto.AttendanceResponse;
import com.example.demo.attendance.dto.ClockInRequest;
import com.example.demo.attendance.dto.ClockOutRequest;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/attendance")
@AllArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public AttendanceResponse clockIn(@Valid @RequestBody ClockInRequest request) {
        return attendanceService.clockIn(request);
    }

    @PostMapping("/clock-out")
    public AttendanceResponse clockOut(@Valid @RequestBody ClockOutRequest request) {
        return attendanceService.clockOut(request);
    }

    @GetMapping("/active")
    public List<ActiveWorkerCacheDto> getActiveWorkers() {
        return attendanceService.getActiveWorkers();
    }

    @GetMapping("/log")
    public Page<AttendanceResponse> getAttendanceLog(
            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return attendanceService.getAttendanceLog(workerId, from, to, page, size);
    }
}