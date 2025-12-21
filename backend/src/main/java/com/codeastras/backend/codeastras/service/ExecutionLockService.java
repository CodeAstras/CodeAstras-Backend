package com.codeastras.backend.codeastras.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExecutionLockService {

    private final ConcurrentHashMap<UUID, AtomicBoolean> locks =
            new ConcurrentHashMap<>();

    public boolean tryLock(UUID projectId) {
        return locks
                .computeIfAbsent(projectId, k -> new AtomicBoolean(false))
                .compareAndSet(false, true);
    }

    public void unlock(UUID projectId) {
        AtomicBoolean lock = locks.get(projectId);
        if (lock != null) {
            lock.set(false);
        }
    }
}
