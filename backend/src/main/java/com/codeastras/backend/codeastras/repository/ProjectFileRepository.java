package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {

    List<ProjectFile> findByProjectId(UUID projectId);

    ProjectFile findByProjectIdAndPath(UUID projectId, String path);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ProjectFile f SET f.content = :content, f.updatedAt = CURRENT_TIMESTAMP WHERE f.projectId = :projectId AND f.path = :path")
    void updateContent(@Param("projectId") UUID projectId, @Param("path") String path, @Param("content") String content);

    boolean existsByProjectIdAndPath(UUID projectId, String path);
}
