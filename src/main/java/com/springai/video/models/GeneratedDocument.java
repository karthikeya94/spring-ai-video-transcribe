package com.springai.video.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "generated_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_transcript_id")
    private VideoTranscript videoTranscript;
    
    @Column(name = "document_type")
    private String documentType;
    
    @Column(columnDefinition = "TEXT")
    private String markdownContent;
    
    @Column(name = "generation_version")
    private String generationVersion;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "evaluation_score")
    private Double evaluationScore;
    
    @CreationTimestamp
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}