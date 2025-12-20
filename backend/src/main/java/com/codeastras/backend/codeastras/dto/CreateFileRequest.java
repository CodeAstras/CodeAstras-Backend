package com.codeastras.backend.codeastras.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateFileRequest {

    @NotBlank
    private String path;   // relative path, e.g. src/foo.py

    @NotBlank
    private String type;   // FILE or FOLDER
}
