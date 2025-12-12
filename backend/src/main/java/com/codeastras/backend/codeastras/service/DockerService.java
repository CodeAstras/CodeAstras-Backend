package com.codeastras.backend.codeastras.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class DockerService {

    @Value("${code.runner.image-name:py-collab-runner}")
    private String imageName;

    public int runContainer(String containerName, String sessionHostPath) throws Exception {
        List<String> cmd = List.of(
                "docker", "run", "-d",
                "--name", containerName,
                "--memory=512m",
                "--cpus=1",
                "--pids-limit=256",
                "--network=none",
                "-v", sessionHostPath + ":/workspace",
                imageName
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Docker run failed (exit " + exit + "): " + output.toString());
        }

        return exit;
    }

}