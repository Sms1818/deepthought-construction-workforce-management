package com.example.demo.attendance;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.attendance.dto.ActiveWorkerCacheDto;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ActiveWorkerCacheService {
    private static final String ACTIVE_WORKER_KEY_PREFIX = "active_worker:";
    private static final Duration ACTIVE_WORKER_TTL = Duration.ofHours(16);

    private final RedisTemplate<String, ActiveWorkerCacheDto> redisTemplate;

    public void addActiveWorker(ActiveWorkerCacheDto dto) {
        String key = buildKey(dto.getWorkerId());
        redisTemplate.opsForValue().set(key, dto, ACTIVE_WORKER_TTL);
    }

    public void removeActiveWorker(Long workerId) {
        redisTemplate.delete(buildKey(workerId));
    }

    public List<ActiveWorkerCacheDto> getActiveWorkers() {
        Set<String> keys = redisTemplate.keys(ACTIVE_WORKER_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String buildKey(Long workerId) {
        return ACTIVE_WORKER_KEY_PREFIX + workerId;
    }

}
