package com.example.demo.attendance;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.attendance.dto.ActiveWorkerCacheDto;

import io.lettuce.core.RedisConnectionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ActiveWorkerCacheService {
    private static final String ACTIVE_WORKER_KEY_PREFIX = "active_worker:";
    private static final Duration ACTIVE_WORKER_TTL = Duration.ofHours(16);

    private final RedisTemplate<String, ActiveWorkerCacheDto> redisTemplate;

    public void addActiveWorker(ActiveWorkerCacheDto dto) {
        try {
            String key = buildKey(dto.getWorkerId());
            redisTemplate.opsForValue().set(key, dto, ACTIVE_WORKER_TTL);
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable. Skipping active worker cache write for workerId=", dto.getWorkerId());

        }
    }

    public void removeActiveWorker(Long workerId) {
        try {
            redisTemplate.delete(buildKey(workerId));
        } catch (RedisConnectionException ex) {
            log.warn("Redis unavailable. Skipping active worker cache delete for workerId=", workerId);
        }
    }

    public List<ActiveWorkerCacheDto> getActiveWorkers() {
        try {
            Set<String> keys = redisTemplate.keys(ACTIVE_WORKER_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            return keys.stream()
                    .map(key -> redisTemplate.opsForValue().get(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (RedisConnectionFailureException ex) {
            log.warn("Redis unavailable. Returning empty active worker cache response.");
            return Collections.emptyList();
        }
    }

    private String buildKey(Long workerId) {
        return ACTIVE_WORKER_KEY_PREFIX + workerId;
    }

}
