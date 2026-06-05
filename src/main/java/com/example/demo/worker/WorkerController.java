package com.example.demo.worker;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.worker.dto.CreateWorkerRequest;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/workers")
@AllArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping
    public Worker createWorker(@RequestBody CreateWorkerRequest worker) {
        return workerService.createWorker(worker);
    }

    @GetMapping
    public List<Worker> getWorkers() {
        return workerService.getWorkers();
    }
}