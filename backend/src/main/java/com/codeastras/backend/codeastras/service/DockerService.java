package com.codeastras.backend.codeastras.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

        Process p = new ProcessBuilder(cmd).start();
        p.waitFor();

        return p.exitValue();
    }

    public int stopContainer(String containerName) throws Exception {
        Process p = new ProcessBuilder("docker", "rm", "-f", containerName).start();
        p.waitFor();
        return p.exitValue();
    }
}