package com.skillforge.app.repository;

import com.skillforge.app.model.TopicVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicVideoRepository extends JpaRepository<TopicVideo, Long> {
    List<TopicVideo> findByTopic(String topic);
}
