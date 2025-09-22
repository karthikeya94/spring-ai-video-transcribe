package com.springai.video.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequest {
    private String inputType;
    private String content;
    private String sessionId;
    private VideoTranscript videoTranscript;
    private String contentClassification;
    private String domainSpecialistAnalysis;
    private Map<String,Object> generatedContent;
    private String documentationContent;
    private String merged;
    private String primaryDomain;
    private ContentBundle contentBundle;
}