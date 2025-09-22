package com.springai.video.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdate {
    private String sessionId;
    private String stage;
    private String message;
    private Integer percentComplete;
    private LocalDateTime timestamp;
    private String status;
    
    public static ProgressUpdate create(String sessionId, String stage, String message, Integer percentComplete) {
        return new ProgressUpdate(sessionId, stage, message, percentComplete, LocalDateTime.now(), "PROCESSING");
    }
    
    public static ProgressUpdate completed(String sessionId, String message) {
        return new ProgressUpdate(sessionId, "COMPLETED", message, 100, LocalDateTime.now(), "COMPLETED");
    }
    
    public static ProgressUpdate error(String sessionId, String message) {
        return new ProgressUpdate(sessionId, "ERROR", message, 0, LocalDateTime.now(), "ERROR");
    }
}