package com.springai.video.repo;

import com.springai.video.models.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {

    Optional<GeneratedDocument> findByVideoTranscriptIdAndDocumentTypeAndIsActive(
            Long videoTranscriptId, String documentType, Boolean isActive);

}