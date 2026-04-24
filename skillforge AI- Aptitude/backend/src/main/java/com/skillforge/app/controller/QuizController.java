package com.skillforge.app.controller;

import com.skillforge.app.dto.QuizSubmitDto;
import com.skillforge.app.model.Question;
import com.skillforge.app.model.UserProfile;
import com.skillforge.app.repository.UserProfileRepository;
import com.skillforge.app.service.AdaptiveQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*") // Allows calls from Vanilla HTML/JS port
public class QuizController {

    @Autowired
    private AdaptiveQuizService quizService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/next/{userId}")
    public ResponseEntity<?> getNextQuestion(@PathVariable Long userId) {
        Optional<Question> questionOpt = quizService.getNextAdaptiveQuestion(userId);
        if (questionOpt.isPresent()) {
            return ResponseEntity.ok(questionOpt.get());
        } else {
            return ResponseEntity.status(404).body("No more questions available for your level.");
        }
    }

    @PostMapping("/submit/{userId}")
    public ResponseEntity<?> submitAnswer(@PathVariable Long userId, @RequestBody QuizSubmitDto request) {
        try {
            Map<String, Object> result = quizService.submitAnswer(
                    userId, 
                    request.getQuestionId(), 
                    request.getSelectedOption(), 
                    request.getResponseTimeMs()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfileData(@PathVariable Long userId) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        if (profileOpt.isPresent()) {
            Map<String, Double> topicMastery = quizService.getTopicMastery(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("profile", profileOpt.get());
            response.put("topicMastery", topicMastery);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body("Profile not found");
        }
    }
}
