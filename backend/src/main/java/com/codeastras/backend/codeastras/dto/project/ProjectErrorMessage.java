package com.codeastras.backend.codeastras.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectErrorMessage {
    private final String error;
    private final String message;
}
