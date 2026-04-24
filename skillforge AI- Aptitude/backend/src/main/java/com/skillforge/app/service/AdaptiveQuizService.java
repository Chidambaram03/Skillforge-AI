package com.skillforge.app.service;

import com.skillforge.app.model.Attempt;
import com.skillforge.app.model.Question;
import com.skillforge.app.model.User;
import com.skillforge.app.model.UserProfile;
import com.skillforge.app.repository.AttemptRepository;
import com.skillforge.app.repository.QuestionRepository;
import com.skillforge.app.repository.UserProfileRepository;
import com.skillforge.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveQuizService {

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AttemptRepository attemptRepository;
    @Autowired private OpenAIService openAIService;
    @Autowired private RandomizedQuestionEngine questionEngine;
    @Autowired private TopicDistributionService topicDistributionService;

    // Time within which a correct answer earns a promotion
    private static final long FAST_THRESHOLD_MS = 20000;

    /**
     * Core adaptive question selector.
     *
     * Rules:
     *  1. Cycle through all 17 topics evenly — topic with fewest correct answers goes first.
     *  2. If the user got the last question WRONG on a topic, repeat that SAME topic (not question).
     *  3. Never show the same question type/text if the user answered it correctly.
     *  4. Difficulty rises with level; OpenAI generates genuinely unique hard questions.
     */
    public Optional<Question> getNextAdaptiveQuestion(Long userId) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
        if (!profileOpt.isPresent()) return Optional.empty();

        UserProfile profile = profileOpt.get();
        String level = profile.getThinkingLevel();

        // --- Choose topic ---
        String topic = chooseTopic(userId);

        // --- Gather seen question texts to avoid repetition ---
        List<String> seenTexts = getSeenQuestionTexts(userId);

        // --- Generate via OpenAI (or fallback engine) ---
        Question q = openAIService.generateQuestion(topic, level, seenTexts);
        q.setTopic(topic);
        q.setDifficultyLevel(level);

        Question saved = questionRepository.save(q);
        return Optional.of(saved);
    }

    /**
     * Choose the next topic.
     * - If the last attempt was WRONG, return the same topic (reinforce weakness).
     * - Otherwise, pick the topic where the user has the fewest CORRECT answers
     *   so all 17 topics are covered evenly.
     */
    private String chooseTopic(Long userId) {
        List<Attempt> history = attemptRepository.findByUserId(userId);

        // Check if last attempt was wrong — if so, repeat that topic
        if (!history.isEmpty()) {
            Attempt last = history.stream()
                .max(Comparator.comparing(a -> a.getCreatedAt()))
                .orElse(null);
            if (last != null && !Boolean.TRUE.equals(last.getIsCorrect())) {
                return normaliseTopic(last.getQuestion().getTopic());
            }
        }

        // Count correct attempts per topic
        Map<String, Long> correctPerTopic = new HashMap<>();
        for (Attempt a : history) {
            if (Boolean.TRUE.equals(a.getIsCorrect())) {
                String t = normaliseTopic(a.getQuestion().getTopic());
                correctPerTopic.merge(t, 1L, Long::sum);
            }
        }

        // Pick the topic with fewest correct answers (ensures full coverage)
        return TopicDistributionService.ALL_TOPICS.stream()
            .min(Comparator.comparingLong(t -> correctPerTopic.getOrDefault(t, 0L)))
            .orElse("Percentage");
    }

    /** Returns a list of question texts the user has already answered CORRECTLY. */
    private List<String> getSeenQuestionTexts(Long userId) {
        return attemptRepository.findByUserId(userId).stream()
            .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
            .map(a -> a.getQuestion().getQuestionText())
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    private String normaliseTopic(String raw) {
        if (raw == null) return "Percentage";
        for (String t : TopicDistributionService.ALL_TOPICS) {
            if (raw.equalsIgnoreCase(t)) return t;
        }
        // fuzzy match on first word
        String first = raw.split(" ")[0].toLowerCase();
        return TopicDistributionService.ALL_TOPICS.stream()
            .filter(t -> t.toLowerCase().startsWith(first))
            .findFirst().orElse(raw);
    }

    // ─── Submit Answer ────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> submitAnswer(Long userId, Long questionId,
                                             String selectedOption, long responseTimeMs) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Question not found"));
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(selectedOption.trim());

        // Record attempt
        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setQuestion(question);
        attempt.setSelectedOption(selectedOption);
        attempt.setIsCorrect(isCorrect);
        attempt.setResponseTimeMs(responseTimeMs);
        attemptRepository.save(attempt);

        // Level progression
        String prevLevel = profile.getThinkingLevel();
        String nextLevel = prevLevel;
        String feedback;

        if (isCorrect) {
            if (responseTimeMs <= FAST_THRESHOLD_MS) {
                nextLevel = promoteLevel(prevLevel);
                feedback = "Excellent! Promoted to level " + nextLevel + ". Harder questions ahead!";
            } else {
                feedback = "Correct! Improve your speed to unlock level " + promoteLevel(prevLevel) + ".";
            }
        } else {
            nextLevel = demoteLevel(prevLevel);
            feedback = "Incorrect. The platform will give you more practice on "
                + question.getTopic() + " before moving forward.";
        }

        profile.setThinkingLevel(nextLevel);
        updateProfileStats(profile, userId);
        userProfileRepository.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("isCorrect", isCorrect);
        response.put("correctOption", question.getCorrectOption());
        response.put("explanation", question.getExplanation());
        response.put("previousLevel", prevLevel);
        response.put("newLevel", nextLevel);
        response.put("feedback", feedback);
        response.put("topic", question.getTopic());
        response.put("levelChanged", !prevLevel.equals(nextLevel));
        return response;
    }

    public Map<String, Double> getTopicMastery(Long userId) {
        return topicDistributionService.getTopicAccuracy(userId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String promoteLevel(String l) {
        switch (l) { case "L1": return "L2"; case "L2": return "L3"; case "L3": return "L4"; default: return "L4"; }
    }

    private String demoteLevel(String l) {
        switch (l) { case "L4": return "L3"; case "L3": return "L2"; case "L2": return "L1"; default: return "L1"; }
    }

    private void updateProfileStats(UserProfile profile, Long userId) {
        List<Attempt> all = attemptRepository.findByUserId(userId);
        if (all.isEmpty()) return;
        long correct = all.stream().filter(Attempt::getIsCorrect).count();
        double acc = ((double) correct / all.size()) * 100.0;
        double avg = all.stream().mapToLong(Attempt::getResponseTimeMs).average().orElse(0);
        profile.setAccuracyPercentage(Math.round(acc * 10.0) / 10.0);
        profile.setAvgResponseTimeMs(Math.round(avg * 10.0) / 10.0);
    }
}
