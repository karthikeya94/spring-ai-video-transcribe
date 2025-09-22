package com.springai.video.agents;

import com.springai.video.util.AgentPrompts;
import com.springai.video.models.ProcessingRequest;
import com.springai.video.util.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class Evaluator {
    private final ChatClient chatClient;
    private final Utils util;

    public void executeIterativeImprovement(ProcessingRequest request) {
        int MAX_IMPROVEMENT_ITERATIONS = 3;
        util.sendProgress(request.getSessionId(), "IMPROVEMENT", " Applying quality improvements...", 95);
        String currentDocument = request.getDocumentationContent();

        try {
            for (int iteration = 1; iteration <= MAX_IMPROVEMENT_ITERATIONS; iteration++) {
                log.info("Starting improvement iteration {} for session: {}", iteration, request.getSessionId());

                String evaluation = evaluateDocument(currentDocument, request.getSessionId(), iteration);

                if (isDocumentSatisfactory(evaluation)) {
                    log.info("Document quality approved after {} iteration(s)", iteration);
                    util.sendProgress(request.getSessionId(), "EVALUATION", "EvaluationComplete"+String.format(" Document quality approved after %d iteration(s)", iteration), 92);
                    break;
                }

               util.sendProgress(request.getSessionId(), "IMPROVEMENT", "DocumentOptimizer"+String.format("🔄 Applying improvements - Iteration %d/%d", iteration, MAX_IMPROVEMENT_ITERATIONS), 95 + iteration);

                currentDocument = optimizeDocument(currentDocument, evaluation, request.getSessionId(), iteration);

                if (iteration == MAX_IMPROVEMENT_ITERATIONS) {
                    log.info("Maximum improvement iterations reached for session: {}", request.getSessionId());
                    util.sendProgress(request.getSessionId(), "IMPROVEMENT", "ImprovementComplete Document optimization completed (max iterations reached)", 98);
                }
            }
            request.setDocumentationContent(currentDocument);
        } catch (Exception e) {
            log.error("Evaluation and improvement failed for session: {}", request.getSessionId(), e);
        }
    }

    private String evaluateDocument(String document, String sessionId, int iteration) {
        String evaluationPrompt = String.format("""
        Evaluate this learning document for master-level effectiveness (Iteration %d):

        **Document to Evaluate:**
        %s

        Provide comprehensive evaluation following your assessment framework.
        Focus particularly on the document's ability to enable single-read mastery.
        """, iteration, document);

        return chatClient.prompt()
                .system(AgentPrompts.DOCUMENT_EVALUATOR_SYSTEM_PROMPT)
                .user(evaluationPrompt)
                .call()
                .content();
    }


    private String optimizeDocument(String document, String evaluation, String sessionId, int iteration) {
        String optimizationPrompt = String.format("""
        Optimize this learning document based on evaluation feedback (Iteration %d):

        **Current Document:**
        %s

        **Evaluation Feedback:**
        %s

        Apply all feedback to create an improved version that addresses all concerns while enhancing overall learning effectiveness.
        Focus on enabling true single-read mastery while maintaining comprehensive coverage.
        """, iteration, document, evaluation);

        return chatClient.prompt()
                .system(AgentPrompts.DOCUMENT_OPTIMIZER_SYSTEM_PROMPT)
                .user(optimizationPrompt)
                .call()
                .content();
    }

    private boolean isDocumentSatisfactory(String evaluation) {
        String evaluationLower = evaluation.toLowerCase();
        return !evaluationLower.contains("critical") &&
                !evaluationLower.contains("must fix") &&
                !evaluationLower.contains("prevents mastery") &&
                (evaluationLower.contains("mastery_enablement_score: 9") ||
                        evaluationLower.contains("mastery_enablement_score: 10"));
    }

}
