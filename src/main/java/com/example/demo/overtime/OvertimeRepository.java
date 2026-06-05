package com.example.demo.overtime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OvertimeRepository extends JpaRepository<OvertimeEntry, Long> {
}