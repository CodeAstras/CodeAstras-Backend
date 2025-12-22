package com.codeastras.backend.codeastras.service;

@FunctionalInterface
public interface RunOutputSink {
    void onOutput(String output);
}
