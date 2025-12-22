package com.codeastras.backend.codeastras.exception;

public class DockerCommandException extends RuntimeException {
    public DockerCommandException(String message) {
        super(message);
    }
}
