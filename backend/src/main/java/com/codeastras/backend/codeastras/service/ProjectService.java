package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.entity.ProjectFile;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.repository.ProjectFileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectFileRepository fileRepo;

    public ProjectService(ProjectRepository projectRepo, ProjectFileRepository fileRepo) {
        this.projectRepo = projectRepo;
        this.fileRepo = fileRepo;
    }

    public Project createProject(CreateProjectRequest req) {

        // Create project entry
        Project project = new Project(
                UUID.randomUUID(),
                req.getName(),
                req.getLanguage()
        );

        projectRepo.save(project);

        // Python default files
        fileRepo.save(new ProjectFile(
                UUID.randomUUID(),
                project.getId(),
                "/main.py",
                "print(\"Hello from CodeAstras!\")",
                "FILE"
        ));

        fileRepo.save(new ProjectFile(
                UUID.randomUUID(),
                project.getId(),
                "/requirements.txt",
                "",
                "FILE"
        ));

        fileRepo.save(new ProjectFile(
                UUID.randomUUID(),
                project.getId(),
                "/README.md",
                "# " + project.getName(),
                "FILE"
        ));

        return project;
    }
}
