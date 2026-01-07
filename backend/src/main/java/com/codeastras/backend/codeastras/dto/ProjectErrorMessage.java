package com.codeastras.backend.codeastras.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectErrorMessage {
    private final String error;
    private final String message;
}
