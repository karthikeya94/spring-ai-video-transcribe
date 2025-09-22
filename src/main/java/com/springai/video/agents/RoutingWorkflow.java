package com.springai.video.agents;

import com.springai.video.util.AgentPrompts;
import com.springai.video.models.*;
import com.springai.video.util.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class RoutingWorkflow {
    private final ChatClient chatClient;
    private final Utils util;

    public void executeDomainSpecialistAnalysis(ProcessingRequest request) {

        util.sendProgress(request.getSessionId(), "SPECIALIST_ANALYSIS", " Engaging %s specialist for deep analysis...", 25);

        try {
            String classificationPrompt = String.format("""
            Analyze this video transcript and classify it for comprehensive learning document creation:

            **Transcript Content:**
            %s
            
            Provide detailed domain classification following your analysis framework.
            Return clear, structured analysis that will guide subsequent content generation agents.
            """,
                    request.getVideoTranscript().getRawTranscript());

            String classification = chatClient.prompt()
                    .system(AgentPrompts.CONTENT_CLASSIFIER_SYSTEM_PROMPT)
                    .user(classificationPrompt)
                    .call()
                    .content();

            String domain = extractPrimaryDomain(classification);
            util.sendProgress(request.getSessionId(), "CLASSIFICATION", "ContentClassifierAgent"+ String.format(" Content classified as %s domain", domain), 25);
            request.setPrimaryDomain(domain);
            request.setContentClassification(classification);
            executeRoutingWorkflowPattern(request);

        } catch (Exception e) {
            log.error("Content classification failed for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Content classification failed", e);
        }
    }

    private String extractPrimaryDomain(String classification) {
        try {
            String[] lines = classification.split("\\n");
            for (String line : lines) {
                if (line.startsWith("PRIMARY_DOMAIN:")) {
                    return line.substring("PRIMARY_DOMAIN:".length()).trim();
                }
            }
            return "TECHNICAL";
        } catch (Exception e) {
            log.warn("Could not extract primary domain, using default", e);
            return "TECHNICAL";
        }
    }


    private String getSpecialistPrompt(String domain) {
        return switch (domain.toUpperCase()) {
            case "TECHNICAL" -> AgentPrompts.TECHNICAL_SPECIALIST_SYSTEM_PROMPT;
            case "MATHEMATICS" -> AgentPrompts.MATHEMATICS_SPECIALIST_SYSTEM_PROMPT;
            case "SCIENCE" -> AgentPrompts.SCIENCE_SPECIALIST_SYSTEM_PROMPT;
            case "BUSINESS" -> AgentPrompts.BUSINESS_SPECIALIST_SYSTEM_PROMPT;
            default -> AgentPrompts.DEFAULT_SPECIALIST_SYSTEM_PROMPT;
        };
    }


    public void executeRoutingWorkflowPattern(ProcessingRequest request) {
        util.sendProgress(request.getSessionId(), "ROUTING", "Classifying and routing content sections", 30);
        try {
            String specialistPrompt = getSpecialistPrompt(request.getPrimaryDomain());

            String analysisPrompt = String.format("""
            As a %s specialist, analyze this content for comprehensive master-level learning document creation:

            **Content Classification:**
            %s

            **Video Content to Analyze:**
            **Transcript:** %s

            Provide your specialist analysis following your required output format.
            Focus on creating a learning pathway that enables true mastery through structured progression.
            """,
                    request.getPrimaryDomain(),
                    request.getContentClassification(),
                    request.getVideoTranscript().getRawTranscript());

            String specialistAnalysis = chatClient.prompt()
                    .system(specialistPrompt)
                    .user(analysisPrompt)
                    .call()
                    .content();

            util.sendProgress(request.getSessionId(), "SPECIALIST_ANALYSIS", "DomainSpecialistAgent"+String.format(" %s specialist analysis completed", request.getPrimaryDomain()), 35);

            request.setDomainSpecialistAnalysis(specialistAnalysis);

        } catch (Exception e) {
            log.error("Domain specialist analysis failed for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Domain specialist analysis failed", e);
        }
    }

}
