package com.springai.video.agents;

import com.springai.video.util.AgentPrompts;
import com.springai.video.models.ContentBundle;
import com.springai.video.models.ProcessingRequest;
import com.springai.video.models.VideoTranscript;
import com.springai.video.util.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@AllArgsConstructor
public class ParallelizationWorkflow {
    private final ChatClient chatClient;
    private final Utils util;


    public void executeMultiAgentContentGeneration(ProcessingRequest request) {
        util.sendProgress(request.getSessionId(), "CONTENT_GENERATION", "Coordinating specialized content generation agents...", 45);
        try {
            List<CompletableFuture<String>> contentTasks = Arrays.asList(
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return generateTheoryContent(request.getVideoTranscript(), request.getContentClassification(), request.getDomainSpecialistAnalysis(), request.getSessionId());
                        } catch (Exception e) {
                            log.error("Theory generation failed", e);
                            return "Error generating theory content: " + e.getMessage();
                        }
                    }),
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return generateCodeExamples(request.getVideoTranscript(), request.getContentClassification(), request.getDomainSpecialistAnalysis(), request.getSessionId());
                        } catch (Exception e) {
                            log.error("Code generation failed", e);
                            return "Error generating code examples: " + e.getMessage();
                        }
                    }),
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return generatePracticalApplications(request.getVideoTranscript(), request.getContentClassification(), request.getDomainSpecialistAnalysis(), request.getSessionId());
                        } catch (Exception e) {
                            log.error("Applications generation failed", e);
                            return "Error generating practical applications: " + e.getMessage();
                        }
                    })
            );

            List<String> results = contentTasks.stream()
                    .map(CompletableFuture::join)
                    .toList();

            ContentBundle contentBundle = new ContentBundle(
                    results.get(0),
                    results.get(1),
                    results.get(2)
            );

            util.sendProgress(request.getSessionId(), "CONTENT_GENERATION", "ParallelContentGenerators All specialized content generated successfully", 65);

            request.setContentBundle(contentBundle);

        } catch (Exception e) {
            log.error("Parallel content generation failed for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Content generation failed", e);
        }
    }

    private String generateTheoryContent(VideoTranscript videoTranscript, String contentClassification, String domainAnalysis, String sessionId) {
        util.sendProgress(sessionId, "CONTENT_GENERATION", "TheoryContentGenerator Generating comprehensive theory explanations...", 50);
        String theoryPrompt = String.format("""
                        Generate comprehensive theory content for master-level learning:
                        
                        **Content Classification:**
                        %s
                        
                        **Domain Specialist Analysis:**
                        %s
                        
                        **Original Video Content:**
                        **Transcript:** %s
                        
                        Create theory content following your structured approach that enables mastery through progressive understanding.
                        Focus on industry-standard explanations with current 2025 practices and examples.
                        """,
                contentClassification,
                domainAnalysis,
                videoTranscript.getRawTranscript());

        return chatClient.prompt()
                .system(AgentPrompts.THEORY_CONTENT_GENERATOR_PROMPT)
                .user(theoryPrompt)
                .call()
                .content();
    }

    private String generateCodeExamples(VideoTranscript videoTranscript, String contentClassification, String domainAnalysis, String sessionId) {
        util.sendProgress(sessionId, "CONTENT_GENERATION", "CodeExamplesGenerator Creating industry-standard code examples...", 55);
        String codePrompt = String.format("""
                        Generate comprehensive code examples for master-level learning:
                        
                        **Content Classification:**
                        %s
                        
                        **Domain Specialist Analysis:**
                        %s
                        
                        **Original Video Content:**
                        **Transcript:** %s
                        
                        Create progressive code examples following your structured approach.
                        Include current 2025 industry standards, best practices, and production-ready implementations.
                        If the content is not code-focused, create relevant technical demonstrations or skip with explanation.
                        """,
                contentClassification,
                domainAnalysis,
                videoTranscript.getRawTranscript());

        return chatClient.prompt()
                .system(AgentPrompts.CODE_EXAMPLES_GENERATOR_PROMPT)
                .user(codePrompt)
                .call()
                .content();
    }

    private String generatePracticalApplications(VideoTranscript videoTranscript, String contentClassification,  String domainAnalysis, String sessionId) {
        util.sendProgress(sessionId, "CONTENT_GENERATION", "PracticalApplicationsGenerator Creating real-world practical applications...", 60);
        String applicationsPrompt = String.format("""
                        Generate comprehensive practical applications for master-level learning:
                        
                        **Content Classification:**
                        %s
                        
                        **Domain Specialist Analysis:**
                        %s
                        
                        **Original Video Content:**
                        **Transcript:** %s
                        
                        Create progressive practical applications following your structured approach.
                        Focus on real-world industry applications with current 2025 standards and professional use cases.
                        """,
                contentClassification,
                domainAnalysis,
                videoTranscript.getRawTranscript());

        return chatClient.prompt()
                .system(AgentPrompts.PRACTICAL_APPLICATIONS_GENERATOR_PROMPT)
                .user(applicationsPrompt)
                .call()
                .content();
    }

}
