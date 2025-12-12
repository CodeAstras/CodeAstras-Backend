package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {

    List<ProjectFile> findByProjectId(UUID projectId);

    ProjectFile findByProjectIdAndPath(UUID projectId, String path);

    boolean existsByProjectIdAndPath(UUID projectId, String path);
}
