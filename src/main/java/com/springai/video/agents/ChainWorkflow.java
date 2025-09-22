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
public class ChainWorkflow {
    private final ChatClient chatClient;
    private final Utils util;

    public void executeChainWorkflowMerging(ProcessingRequest request) {

        util.sendProgress(request.getSessionId(), "CHAIN_WORKFLOW", "ContentMergerGapFiller Intelligently merging content and filling gaps for seamless learning...", 70);

        try {
            String mergingPrompt = String.format("""
            Analyze, merge, and enhance the parallel content to create seamless, comprehensive learning material:

            **Content Classification:**
            %s

            **Domain Analysis:**
            %s

            **Generated Parallel Content to Merge:**
            
            **THEORY CONTENT:**
            %s

            **CODE EXAMPLES:**
            %s

            **PRACTICAL APPLICATIONS:**
            %s

            **Your Critical Task:**
            1. Analyze all content for gaps, duplications, and missing connections
            2. Fill gaps where theory exists but examples are missing (create code examples)
            3. Fill gaps where examples exist but theory is insufficient (add theoretical foundation)
            4. Merge content intelligently without duplication
            5. Create smooth transitions and cross-references
            6. Add real-world industry context where missing
            7. Ensure progressive learning from basic to advanced mastery

            Generate a comprehensive, integrated content structure that enables single-read mastery.
            """,
                    request.getDocumentationContent(),
                    request.getDomainSpecialistAnalysis(),
                    request.getContentBundle().theoryContent(),
                    request.getContentBundle().codeContent(),
                    request.getContentBundle().applicationsContent());

            String mergedContent = chatClient.prompt()
                    .system(AgentPrompts.CONTENT_MERGER_GAP_FILLER_PROMPT)
                    .user(mergingPrompt)
                    .call()
                    .content();

            util.sendProgress(request.getSessionId(), "CHAIN_WORKFLOW", "ContentMergerGapFiller Content intelligently merged with gaps filled successfully", 75);

            request.setMerged(mergedContent);
            executeDocumentationAssembly(request);
        } catch (Exception e) {
            log.error("Chain workflow merging failed for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Content merging and gap filling failed", e);
        }
    }

    private void executeDocumentationAssembly(ProcessingRequest request) {

        util.sendProgress(request.getSessionId(), "ASSEMBLY", " Assembling comprehensive documentation...", 75);

        try {
            String assemblyPrompt = String.format("""
            Create the final comprehensive master-level learning document from the merged, gap-filled content:

            **Content Classification:**
            %s

            **Merged & Gap-Filled Integrated Content:**
            %s

            Transform this integrated content into a complete, cohesive document following your structured format.
            Ensure the document enables single-read mastery with seamless progression from basic to advanced concepts.
            Focus on creating a definitive guide that transforms novices into experts through comprehensive understanding.
            """,
                    request.getContentClassification(),
                    request.getMerged());

            String assembledDocument = chatClient.prompt()
                    .system(AgentPrompts.DOCUMENTATION_ASSEMBLY_AGENT_PROMPT)
                    .user(assemblyPrompt)
                    .call()
                    .content();

            util.sendProgress(request.getSessionId(), "DOCUMENT_ASSEMBLY", "DocumentAssemblyAgent Comprehensive master-level document assembled successfully", 90);

            request.setDocumentationContent(assembledDocument);

        } catch (Exception e) {
            log.error("Document assembly failed for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Document assembly failed", e);
        }
    }

}
