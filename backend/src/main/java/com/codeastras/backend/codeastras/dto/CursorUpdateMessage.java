package com.codeastras.backend.codeastras.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CursorUpdateMessage {

    private UUID projectId;
    private UUID fileId;
    private UUID userId;

    private int line;
    private int column;

    // nullable selection
    private Integer selectionStartLine;
    private Integer selectionStartColumn;
    private Integer selectionEndLine;
    private Integer selectionEndColumn;

    private long timestamp;
}
