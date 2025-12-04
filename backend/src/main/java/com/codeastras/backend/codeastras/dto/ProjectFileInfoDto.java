package com.codeastras.backend.codeastras.dto;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectFileInfoDto {
    private UUID id;
    private String name;
    private String path;
    private String type; // FILE / FOLDER

    public static ProjectFileInfoDto from(ProjectFile file) {
        return new ProjectFileInfoDto(
                file.getId(),
                extractName(file.getPath()),
                file.getPath(),
                file.getType()
        );
    }

    private static String extractName(String path) {
        if (path == null || path.isBlank()) return "";
        String cleaned = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        if (!cleaned.contains("/")) return cleaned;
        return cleaned.substring(cleaned.lastIndexOf("/") + 1);
    }
}
