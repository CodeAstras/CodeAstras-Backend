package com.codeastras.backend.codeastras.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class FileNodeDto {
    private final String name;
    private final String path;
    private final NodeType type;
    private final Long size;
    private final Instant modifiedAt;
    private final List<FileNodeDto> children;
}
