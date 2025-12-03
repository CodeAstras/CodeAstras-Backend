package com.codeastras.backend.codeastras.store;

import com.codeastras.backend.codeastras.dto.SignupRequest;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectRegistry {

    private static final ConcurrentHashMap<String, SignupRequest.ProjectSession> projects = new ConcurrentHashMap<>();

    public static void add(SignupRequest.ProjectSession projectSession) {
        projects.put(projectSession.getProjectId(), projectSession);
    }

    public static SignupRequest.ProjectSession get(String projectId) {
        return projects.get(projectId);
    }

    public static boolean exists(String projectId) {
        return projects.containsKey(projectId);
    }
}
