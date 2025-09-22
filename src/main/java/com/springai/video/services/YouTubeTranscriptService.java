package com.springai.video.services;

import io.github.thoroldvix.api.TranscriptApiFactory;
import io.github.thoroldvix.api.TranscriptRetrievalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeTranscriptService {
    
    private static final Pattern VIDEO_ID_PATTERN = 
        Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    public String extractVideoId(String youtubeUrl) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid YouTube URL: " + youtubeUrl);
    }

    public String getRawTranscript(String videoId) throws TranscriptRetrievalException {
        var transcripts = TranscriptApiFactory.createDefault().getTranscript(videoId);

        var content = transcripts.getContent();
        log.info("Retrieved transcript with {} segments", content.size());

        StringBuilder transcriptText = new StringBuilder();
        for (var item : content) {
            double startTime = item.getStart();
            String text = item.getText();
            int minutes = (int) (startTime / 60);
            int seconds = (int) (startTime % 60);
            String timestamp = String.format("[%02d:%02d]", minutes, seconds);
            transcriptText.append(timestamp).append(" ").append(text).append("\n");
        }

        return transcriptText.toString();
    }
}
