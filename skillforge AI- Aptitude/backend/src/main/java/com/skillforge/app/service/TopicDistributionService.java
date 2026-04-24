package com.skillforge.app.service;

import com.skillforge.app.model.Attempt;
import com.skillforge.app.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks how many questions a user has seen per topic,
 * and selects the next topic to maximise breadth + target weak areas.
 */
@Service
public class TopicDistributionService {

    public static final List<String> ALL_TOPICS = Arrays.asList(
        "Percentage", "Profit and Loss", "Time and Work", "Time Speed Distance",
        "Ratio and Proportion", "Simple and Compound Interest", "Averages",
        "Mixtures and Allegations", "Permutation", "Combination", "Probability",
        "Number Systems", "Logical Series", "Coding Decoding",
        "Blood Relations", "Direction Sense", "Data Interpretation"
    );

    @Autowired
    private AttemptRepository attemptRepository;

    /**
     * Returns the best topic for the next question.
     * Priority: 1) Weak topics (accuracy < 50%), 2) Least-seen topic.
     */
    public String selectNextTopic(Long userId) {
        List<Attempt> attempts = attemptRepository.findByUserId(userId);

        // Build topic stats
        Map<String, long[]> stats = new HashMap<>(); // [correct, total]
        for (Attempt a : attempts) {
            String topic = normaliseTopic(a.getQuestion().getTopic());
            stats.computeIfAbsent(topic, k -> new long[]{0, 0});
            stats.get(topic)[1]++;
            if (Boolean.TRUE.equals(a.getIsCorrect())) stats.get(topic)[0]++;
        }

        // Find topics with accuracy < 50% (weak) and least attempts
        String weakTopic = null;
        double lowestAccuracy = 1.0;
        String leastSeenTopic = null;
        long minSeen = Long.MAX_VALUE;

        for (String topic : ALL_TOPICS) {
            long[] s = stats.getOrDefault(topic, new long[]{0, 0});
            long total = s[1];
            double acc = total == 0 ? 0.5 : (double) s[0] / total;

            if (total > 0 && acc < 0.5 && acc < lowestAccuracy) {
                lowestAccuracy = acc;
                weakTopic = topic;
            }
            if (total < minSeen) {
                minSeen = total;
                leastSeenTopic = topic;
            }
        }

        // 70% chance to pick weak topic if exists, else least seen
        if (weakTopic != null && Math.random() < 0.7) return weakTopic;
        return leastSeenTopic != null ? leastSeenTopic : ALL_TOPICS.get(new Random().nextInt(ALL_TOPICS.size()));
    }

    /**
     * Returns accuracy per topic for analytics.
     */
    public Map<String, Double> getTopicAccuracy(Long userId) {
        List<Attempt> attempts = attemptRepository.findByUserId(userId);
        Map<String, long[]> stats = new HashMap<>();
        for (Attempt a : attempts) {
            String topic = normaliseTopic(a.getQuestion().getTopic());
            stats.computeIfAbsent(topic, k -> new long[]{0, 0});
            stats.get(topic)[1]++;
            if (Boolean.TRUE.equals(a.getIsCorrect())) stats.get(topic)[0]++;
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (String topic : ALL_TOPICS) {
            long[] s = stats.getOrDefault(topic, new long[]{0, 0});
            double acc = s[1] == 0 ? 0.0 : Math.round(((double) s[0] / s[1]) * 1000.0) / 10.0;
            result.put(topic, acc);
        }
        return result;
    }

    private String normaliseTopic(String raw) {
        if (raw == null) return "Percentage";
        for (String t : ALL_TOPICS) {
            if (raw.toLowerCase().contains(t.toLowerCase().split(" ")[0])) return t;
        }
        return raw;
    }
}
