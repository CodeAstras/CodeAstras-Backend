package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByOwnerIdAndName(UUID ownerId, String name);

}
