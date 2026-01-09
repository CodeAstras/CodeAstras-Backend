package com.codeastras.backend.codeastras.dto.project;

import com.codeastras.backend.codeastras.entity.file.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProjectFileContentDto {
    private UUID id;
    private String path;
    private String content;

    public static ProjectFileContentDto from(ProjectFile file) {
        return new ProjectFileContentDto(
                file.getId(),
                file.getPath(),
                file.getContent()
        );
    }
}
