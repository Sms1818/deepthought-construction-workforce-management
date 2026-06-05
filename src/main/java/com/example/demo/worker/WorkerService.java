package com.example.demo.worker;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ConflictException;
import com.example.demo.worker.dto.CreateWorkerRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;

    public Worker createWorker(CreateWorkerRequest request) {
        workerRepository.findByPhone(request.getPhone())
                .ifPresent(worker -> {
                    throw new ConflictException("Worker with this phone already exists");
                });

        Worker worker = Worker.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .dailyWageRate(request.getDailyWageRate())
                .active(true)
                .build();

        return workerRepository.save(worker);
    }

    public List<Worker> getWorkers() {
        return workerRepository.findAll();
    }
}