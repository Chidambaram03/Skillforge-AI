package com.skillforge.app.controller;

import com.skillforge.app.model.Question;
import com.skillforge.app.model.UserProfile;
import com.skillforge.app.repository.QuestionRepository;
import com.skillforge.app.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diagnostic")
@CrossOrigin(origins = "*")
public class DiagnosticController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "Aptitude API is responding"));
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getDiagnosticQuestions() {
        // Fetch 5 random questions across different levels to test the user
        List<Question> questions = questionRepository.findAll().stream().limit(5).toList();
        if (questions.isEmpty()) {
             return ResponseEntity.status(500).body("Question bank is empty. Load sample data first.");
        }
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/submit/{userId}")
    public ResponseEntity<?> submitDiagnostic(@PathVariable Long userId, @RequestBody Map<String, Integer> results) {
        // results map: score -> number of correct answers (out of 5)
        Integer score = results.get("score");
        
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) return ResponseEntity.notFound().build();

        UserProfile profile = profileOpt.get();
        String initialLevel = "L1";
        
        if (score >= 5) initialLevel = "L4";
        else if (score >= 3) initialLevel = "L3";
        else if (score >= 2) initialLevel = "L2";
        
        profile.setThinkingLevel(initialLevel);
        userProfileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
            "message", "Diagnostic complete. Starting at " + initialLevel,
            "level", initialLevel
        ));
    }
}
