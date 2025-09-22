package com.springai.video.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "video_transcripts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoTranscript {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String videoId;
    
    @Column(name = "video_url")
    private String videoUrl;
    
    @Column(name = "video_title")
    private String videoTitle;
    
    @Column(columnDefinition = "TEXT")
    private String rawTranscript;
    
    @Column(name = "transcript_language")
    private String transcriptLanguage;
    
    @Column(name = "video_duration")
    private Long videoDuration;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "videoTranscript", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeneratedDocument> generatedDocuments;
}