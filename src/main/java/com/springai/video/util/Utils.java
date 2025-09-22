package com.springai.video.util;

import com.springai.video.models.ProgressUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Utils {
    private final SimpMessagingTemplate messagingTemplate;

    public Utils(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendProgress(String sessionId, String stage, String message, Integer percent) {
        ProgressUpdate update = ProgressUpdate.create(sessionId, stage, message, percent);
        messagingTemplate.convertAndSend("/topic/progress/" + sessionId, update);
        log.info("Progress update sent - Session: {}, Stage: {}, Progress: {}%", sessionId, stage, percent);
    }
}
