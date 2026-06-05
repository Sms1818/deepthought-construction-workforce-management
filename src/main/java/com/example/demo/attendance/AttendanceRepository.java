package com.example.demo.attendance;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {
    Optional<AttendanceLog> findByWorkerIdAndClockOutTimeIsNull(Long workerId);

    @EntityGraph(attributePaths = { "worker", "site" })
    Page<AttendanceLog> findByWorkerIdAndClockInTimeBetween(
            Long workerId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

}