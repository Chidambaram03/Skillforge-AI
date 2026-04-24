package com.skillforge.app.service;

import com.skillforge.app.model.TopicVideo;
import com.skillforge.app.repository.TopicVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicVideoService {

    @Autowired
    private TopicVideoRepository topicVideoRepository;

    public List<TopicVideo> getAllVideos() {
        return topicVideoRepository.findAll();
    }

    public List<TopicVideo> getVideosByTopic(String topic) {
        return topicVideoRepository.findByTopic(topic);
    }
    
    public TopicVideo saveVideo(TopicVideo video) {
        return topicVideoRepository.save(video);
    }
}
