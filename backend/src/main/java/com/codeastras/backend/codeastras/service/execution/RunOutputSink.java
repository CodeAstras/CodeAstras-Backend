package com.codeastras.backend.codeastras.service.execution;

@FunctionalInterface
public interface RunOutputSink {
    void onOutput(String output);
}
