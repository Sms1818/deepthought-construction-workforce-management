package com.example.demo.worker;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByPhone(String phone);

}
