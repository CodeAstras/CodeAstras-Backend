package com.codeastras.backend.codeastras.service;

import java.util.UUID;

record PendingEdit(
        UUID projectId,
        String path,
        String content,
        UUID userId
) {}
