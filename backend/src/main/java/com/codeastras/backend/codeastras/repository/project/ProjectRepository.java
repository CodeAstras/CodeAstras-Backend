package com.codeastras.backend.codeastras.repository.project;

import com.codeastras.backend.codeastras.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByOwner_IdAndName(UUID ownerId, String name);

    List<Project> findByOwner_Id(UUID ownerId);
}
