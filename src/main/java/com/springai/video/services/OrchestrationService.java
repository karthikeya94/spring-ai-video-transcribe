package com.springai.video.services;

import com.springai.video.agents.*;
import com.springai.video.models.*;
import com.springai.video.repo.GeneratedDocumentRepository;
import com.springai.video.util.Utils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class OrchestrationService {
    private final ChainWorkflow chainWorkflow;
    private final Evaluator evaluator;
    private final ParallelizationWorkflow parallelizationWorkflow;
    private final RoutingWorkflow routingWorkflow;
    private final GeneratedDocumentRepository documentRepository;
    private final Utils utils;



    public GeneratedDocument orchestrateEnhancedDocumentGeneration(ProcessingRequest request){
        long startTime = System.currentTimeMillis();
        utils.sendProgress(request.getSessionId(), "INITIALIZATION", "Starting enhanced domain-specific analysis...", 25);
        routingWorkflow.executeDomainSpecialistAnalysis(request);
        parallelizationWorkflow.executeMultiAgentContentGeneration(request);
        chainWorkflow.executeChainWorkflowMerging(request);
        evaluator.executeIterativeImprovement(request);
        GeneratedDocument generatedDocument = saveEnhancedDocument(request, startTime);
        utils.sendProgress(request.getSessionId(), "COMPLETED", "Enhanced documentation generation completed successfully!", 100);
        return generatedDocument;
    }


    @Transactional
    private GeneratedDocument saveEnhancedDocument(ProcessingRequest request, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        GeneratedDocument document = new GeneratedDocument();
        document.setVideoTranscript(request.getVideoTranscript());
        document.setDocumentType("ENHANCED_DOMAIN_SPECIFIC");
        document.setMarkdownContent(request.getDocumentationContent());
        document.setGenerationVersion("v2.0-enhanced");
        document.setProcessingTimeMs(processingTime);
        document.setGeneratedAt(LocalDateTime.now());
        document.setIsActive(true);
        return documentRepository.save(document);
    }
}
