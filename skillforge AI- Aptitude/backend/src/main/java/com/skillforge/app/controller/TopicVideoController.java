package com.skillforge.app.controller;

import com.skillforge.app.model.TopicVideo;
import com.skillforge.app.service.TopicVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class TopicVideoController {

    @Autowired
    private TopicVideoService topicVideoService;

    @GetMapping
    public ResponseEntity<List<TopicVideo>> getAllVideos() {
        return ResponseEntity.ok(topicVideoService.getAllVideos());
    }

    @GetMapping("/{topic}")
    public ResponseEntity<List<TopicVideo>> getVideosByTopic(@PathVariable String topic) {
        List<TopicVideo> videos = topicVideoService.getVideosByTopic(topic);
        if (videos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(videos);
    }
}
