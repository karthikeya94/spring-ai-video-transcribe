package com.springai.video.services;

import com.springai.video.models.*;
import com.springai.video.repo.GeneratedDocumentRepository;
import com.springai.video.repo.VideoTranscriptRepository;
import com.springai.video.util.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingService {

    private final YouTubeTranscriptService youTubeService;
    private final OrchestrationService aiOrchestratorService;
    private final VideoTranscriptRepository videoTranscriptRepository;
    private final GeneratedDocumentRepository documentRepository;
    private final Utils util;

    public ProcessingResult processContent(ProcessingRequest request) {
        long startTime = System.currentTimeMillis();
        String sessionId = request.getSessionId();

        try {
            log.info("Processing request - Type: {}, SessionId: {}", request.getInputType(), sessionId);
            util.sendProgress(sessionId, "INIT", "Starting content processing", 0);
            VideoTranscript videoTranscript = handleYouTubeUrl(request.getContent(), sessionId);
            request.setVideoTranscript(videoTranscript);
            Optional<GeneratedDocument> cachedDocument = checkCache(videoTranscript, sessionId);
            if (cachedDocument.isPresent()) {
                return createResultFromCache(cachedDocument.get(), sessionId, startTime);
            }
            GeneratedDocument generatedDocument = aiOrchestratorService.orchestrateEnhancedDocumentGeneration(request);
            long processingTime = System.currentTimeMillis() - startTime;
            ProcessingResult result = new ProcessingResult(
                sessionId,
                generatedDocument.getMarkdownContent(),
                videoTranscript.getVideoTitle(),
                videoTranscript.getVideoId(),
                processingTime,
                generatedDocument.getEvaluationScore(),
                false 
            );
            util.sendProgress(sessionId, "COMPLETED", "Processing completed successfully", 100);
            return result;

        } catch (Exception e) {
            log.error("Error processing content for session {}: {}", sessionId, e.getMessage(), e);
            util.sendProgress(sessionId, "ERROR", "Processing failed: " + e.getMessage(), 0);
            throw new RuntimeException("Content processing failed", e);
        }
    }

    @Transactional
    private VideoTranscript handleYouTubeUrl(String youtubeUrl, String sessionId) {
        util.sendProgress(sessionId, "YOUTUBE", "searching video", 5);
        try {
            String videoId = youTubeService.extractVideoId(youtubeUrl);
            log.info("Extracted video ID: {} from URL: {}", videoId, youtubeUrl);
            Optional<VideoTranscript> existingTranscript = videoTranscriptRepository.findByVideoId(videoId);
            if (existingTranscript.isPresent()) {
                log.info("Using existing transcript for video: {}", videoId);
                return existingTranscript.get();
            }
            
            util.sendProgress(sessionId, "YOUTUBE", "Fetching video ", 10);
            String transcriptText = youTubeService.getRawTranscript(videoId);
            util.sendProgress(sessionId, "YOUTUBE", "Downloading transcript", 15);

            log.info("Fetched transcript for video {}: {} characters", videoId, transcriptText.length());
            VideoTranscript videoTranscript = new VideoTranscript();
            videoTranscript.setVideoId(videoId);
            videoTranscript.setVideoUrl(youtubeUrl);
            videoTranscript.setRawTranscript(transcriptText);
            videoTranscript.setTranscriptLanguage("en");
            videoTranscript.setCreatedAt(LocalDateTime.now());
            
            return videoTranscriptRepository.save(videoTranscript);
            
        } catch (Exception e) {
            log.error("Error handling YouTube URL: {}", e.getMessage());
            throw new RuntimeException("Failed to process YouTube URL: " + e.getMessage(), e);
        }
    }

    private Optional<GeneratedDocument> checkCache(VideoTranscript videoTranscript, String sessionId) {
        util.sendProgress(sessionId, "CACHE", "Checking for cached results", 20);
        log.info("Checking cache for video: {}", videoTranscript.getVideoId());
        try {
            Optional<GeneratedDocument> cachedDoc = documentRepository
                .findByVideoTranscriptIdAndDocumentTypeAndIsActive(videoTranscript.getId(), "FULL_ANALYSIS", true);
            
            if (cachedDoc.isPresent()) {
                log.info("Cache hit for video: {}", videoTranscript.getVideoId());
                util.sendProgress(sessionId, "CACHE", "Found cached result", 50);
            } else {
                log.info("Cache miss for video: {}", videoTranscript.getVideoId());
                util.sendProgress(sessionId, "CACHE", "No cached result found, proceeding with generation", 20);
            }
            
            return cachedDoc;
            
        } catch (Exception e) {
            log.warn("Error checking cache for video {}: {}", videoTranscript.getVideoId(), e.getMessage());
            return Optional.empty();
        }
    }

    private ProcessingResult createResultFromCache(GeneratedDocument cachedDoc, String sessionId, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        
        ProcessingResult result = new ProcessingResult(
            sessionId,
            cachedDoc.getMarkdownContent(),
            cachedDoc.getVideoTranscript().getVideoTitle(),
            cachedDoc.getVideoTranscript().getVideoId(),
            processingTime,
            cachedDoc.getEvaluationScore(),
            true 
        );
        util.sendProgress(sessionId, "COMPLETED", "Retrieved from cache", 100);
        return result;
    }

}