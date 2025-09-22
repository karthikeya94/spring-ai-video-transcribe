package com.springai.video.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    private String sessionId;
    private String markdownContent;
    private String videoTitle;
    private String videoId;
    private Long processingTimeMs;
    private Double evaluationScore;
    private Boolean fromCache;
}