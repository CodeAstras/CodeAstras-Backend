package com.codeastras.backend.codeastras.dto;

public class CommandResult {

    private int exitCode;
    private String output;

    public CommandResult() {}

    public CommandResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}