package com.codeastras.backend.codeastras.dto.project;

import com.codeastras.backend.codeastras.entity.project.Project;
import com.codeastras.backend.codeastras.entity.file.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Getter
public class ProjectResponse {
    private UUID id;
    private String name;
    private String language;
    private List<FileDto> files;
    private String activeSessionId; // nullable

    @Data
    @AllArgsConstructor
    public static class FileDto {
        private UUID id;
        private String name;
        private String path;
    }

    public static ProjectResponse from(Project project, List<ProjectFile> files, String activeSessionId) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .language(project.getLanguage())
                .activeSessionId(activeSessionId)
                .files(files.stream()
                        .map(f -> new FileDto(
                                f.getId(),
                                extractName(f.getPath()),
                                f.getPath()
                        ))
                        .toList())
                .build();
    }

    private static String extractName(String path) {
        if (path == null || path.isBlank()) return "";
        String cleaned = path;
        // remove trailing slash if accidental
        if (cleaned.endsWith("/")) cleaned = cleaned.substring(0, cleaned.length() - 1);
        if (!cleaned.contains("/")) return cleaned;
        return cleaned.substring(cleaned.lastIndexOf("/") + 1);
    }
}
