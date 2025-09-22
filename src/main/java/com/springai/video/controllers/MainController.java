package com.springai.video.controllers;

import com.springai.video.models.*;
import com.springai.video.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final VideoProcessingService videoProcessingService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, ProcessingResult> resultCache = new ConcurrentHashMap<>();

    @GetMapping("/")
    public String index(Model model) {
        String sessionId = UUID.randomUUID().toString();
        model.addAttribute("sessionId", sessionId);
        return "index";
    }

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, String>> processContent(@RequestBody ProcessingRequest request) {
        try {
            log.info("Received processing request - Type: {}", request.getInputType());

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Content cannot be empty"));
            }
            
            if (request.getSessionId() == null) {
                request.setSessionId(UUID.randomUUID().toString());
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    ProcessingResult result = videoProcessingService.processContent(request);
                    resultCache.put(request.getSessionId(), result);
                    ProgressUpdate completion = new ProgressUpdate(
                        request.getSessionId(),
                        "COMPLETED",
                        "Processing completed successfully!",
                        100,
                        java.time.LocalDateTime.now(),
                        "COMPLETED"
                    );
                    messagingTemplate.convertAndSend("/topic/progress/" + request.getSessionId(), completion);
                    
                    return result;
                } catch (Exception e) {
                    log.error("Error in async processing: {}", e.getMessage());
                    
                    // Send error message via WebSocket
                    ProgressUpdate error = ProgressUpdate.error(request.getSessionId(), e.getMessage());
                    messagingTemplate.convertAndSend("/topic/progress/" + request.getSessionId(), error);
                    
                    throw new RuntimeException(e);
                }
            });
            
            return ResponseEntity.ok(Map.of(
                "status", "started",
                "sessionId", request.getSessionId(),
                "message", "Processing started successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Processing failed: " + e.getMessage()));
        }
    }

    @GetMapping("/result/{sessionId}")
    @ResponseBody
    public ResponseEntity<ProcessingResult> getResult(@PathVariable String sessionId) {
        ProcessingResult result = resultCache.get(sessionId);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadMarkdown(@RequestBody Map<String, String> request) {
        try {
            String markdownContent = request.get("content");
            String filename = request.getOrDefault("filename", "generated_document.md");
            
            byte[] contentBytes = markdownContent.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(contentBytes);
                
        } catch (Exception e) {
            log.error("Error creating download: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String sessionId) {
        ProcessingResult result = resultCache.get(sessionId);
        if (result != null) {
            return ResponseEntity.ok(Map.of(
                "status", "completed",
                "hasResult", true
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "status", "processing",
            "hasResult", false
        ));
    }

    @SubscribeMapping("/progress/{sessionId}")
    public ProgressUpdate subscribeToProgress(@DestinationVariable String sessionId) {
        log.info("Client subscribed to progress updates for session: {}", sessionId);
        return ProgressUpdate.create(sessionId, "CONNECTED", "Connected to real-time updates", 0);
    }

    @MessageMapping("/progress/{sessionId}")
    @SendTo("/topic/progress/{sessionId}")
    public ProgressUpdate handleProgressMessage(@DestinationVariable String sessionId, String message) {
        log.debug("Received message for session {}: {}", sessionId, message);
        return ProgressUpdate.create(sessionId, "CLIENT_MESSAGE", message, 0);
    }
}
