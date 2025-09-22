package com.springai.video.repo;

import com.springai.video.models.VideoTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoTranscriptRepository extends JpaRepository<VideoTranscript, Long> {

    Optional<VideoTranscript> findByVideoId(String videoId);

    @Query("SELECT vt FROM VideoTranscript vt LEFT JOIN FETCH vt.generatedDocuments gd " +
           "WHERE vt.videoId = :videoId AND gd.isActive = true")
    Optional<VideoTranscript> findByVideoIdWithActiveDocuments(@Param("videoId") String videoId);
}