package com.codeastras.backend.codeastras.store;

import com.codeastras.backend.codeastras.Model.ProjectSession;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectRegistry {

    private static final ConcurrentHashMap<String, ProjectSession> projects = new ConcurrentHashMap<>();

    public static void add(ProjectSession projectSession) {
        projects.put(projectSession.getProjectId(), projectSession);
    }

    public static ProjectSession get(String projectId) {
        return projects.get(projectId);
    }

    public static boolean exists(String projectId) {
        return projects.containsKey(projectId);
    }
}
