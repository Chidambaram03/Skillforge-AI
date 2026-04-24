package com.skillforge.app.service;

import com.skillforge.app.model.ChatMessage;
import com.skillforge.app.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dynamic ChatService with conversation history + weak-topic personalisation.
 * Works fully without an OpenAI key via an intelligent rule-based engine.
 */
@Service
public class ChatService {

    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private TopicDistributionService topicDistributionService;
    @Autowired private OpenAIService openAIService;

    /**
     * Process a user message. Load history, build context, respond.
     * @param userId  the user ID (for personalisation)
     * @param message the user's raw message
     * @param questionContext the question currently on screen (can be null)
     */
    public String respond(Long userId, String message, Object questionContext) {
        try {
            // 1. Save user message to DB
            saveMessage(userId, "user", message);

            // 2. Load recent history
            List<ChatMessage> history = chatMessageRepository
                    .findTop10ByUserIdOrderByCreatedAtDesc(userId);
            Collections.reverse(history);

            // 3. Get weak topics for personalisation
            Map<String, Double> accuracy = topicDistributionService.getTopicAccuracy(userId);
            List<String> weakTopics = accuracy.entrySet().stream()
                    .filter(e -> e.getValue() > 0 && e.getValue() < 50)
                    .map(Map.Entry::getKey)
                    .limit(3)
                    .collect(Collectors.toList());

            // 4. Build the reply - Try OpenAI first, fallback to Rule Engine
            String reply = openAIService.getChatbotResponse(message, questionContext);
            
            // If OpenAI returns the default mock response (meaning it's not configured or failed),
            // we use our local Dynamic Rule Engine for better context-aware local replies.
            // Updated trigger to check for variants of the offline/mock response.
            if (reply.contains("offline mode") || 
                reply.contains("Configure an OpenAI API key") || 
                reply.contains("Ask me to 'explain this question'")) {
                reply = buildDynamicReply(userId, message, questionContext, weakTopics, history);
            }

            // 5. Save assistant reply to DB
            saveMessage(userId, "assistant", reply);

            return reply;
        } catch (Exception e) {
            System.err.println("[ChatService] Error processing message: " + e.getMessage());
            return "I'm having a little trouble connecting to my specialized logic right now, but I'm still here to help! \n\n" +
                   "Try asking me to **explain** the current question or give you a **hint**. " +
                   "I'll do my best to provide a helpful response!";
        }
    }

    // ─── Dynamic Rule Engine (no API key required) ────────────────────────────

    private String buildDynamicReply(Long userId, String msg, Object ctx,
                                      List<String> weakTopics, List<ChatMessage> history) {
        String m = msg.toLowerCase().trim();
        Random rand = new Random();

        // Topic extraction from context
        String contextTopic = extractTopicFromContext(ctx);

        // 0. Aptitude Filter Removed to allow general queries

        // Helper to check if we just said something
        String lastAssistantMsg = history.isEmpty() ? "" : history.get(history.size()-1).getContent();

        // 1. Greetings
        if (matches(m, "hello", "hi", "hey", "good morning", "good evening")) {
            String[] greets = {
                "Hello! I'm your SkillForge Elite Tutor. Ready to crush some aptitude questions?",
                "Hi there! Let's get to work. What topic are we tackling today?",
                "Greetings! I'm tracking your progress — you're doing well."
            };
            String base = greets[rand.nextInt(greets.length)];
            return base + " " + (weakTopics.isEmpty()
                       ? "You're off to a great start!"
                       : "Your current focus should be: **" + String.join("**, **", weakTopics) + "**.");
        }

        // Formula specific request
        if (matches(m, "formula", "applied", "rule", "method")) {
            if (contextTopic != null) {
                return "The formula for this **" + contextTopic + "** question is:\n\n" + getTopicStrategy(contextTopic);
            }
            return "Tell me which topic you need a formula for, or start a quiz so I can see the question! Generally, remember: Speed = Distance / Time or Profit = SP - CP.";
        }

        // Explain current question
        if (matches(m, "explain", "how", "solve this", "help me", "what is the answer", "tell me", "stuck")) {
            if (ctx != null) {
                if (contextTopic != null) {
                    return "To solve this **" + contextTopic + "** problem, use this formula:\n\n" +
                           getTopicStrategy(contextTopic) + "\n\n" +
                           "💡 **Tip**: Identify the variables from the question and plug them into the formula above!";
                }
                return "I'll help you analyse this question:\n\n" +
                       "📌 **Question Context**: " + summariseContext(ctx) + "\n\n" +
                       "🔍 **Approach**: Identify the type of problem first. Use logic to eliminate wrong options.\n\n" +
                       "💡 **Tip**: Submit your answer to see the full detailed solution!";
            }
            return "To help you with a specific problem, go to the Quiz page. Generally, always identify your variables first!";
        }

        // Hint request
        if (matches(m, "hint", "clue", "tip")) {
            if (ctx != null) {
                String formulaHint = contextTopic != null ? " Try using the " + contextTopic + " formula." : "";
                return "💡 **Quick Hint**: " + summariseContext(ctx) + "\n\n" +
                       "Think about the relationship between the numbers given." + formulaHint + 
                       " Look for key values like rates, percentages, or ratios.";
            }
            return "💡 Pro tip: Always read the question twice. Many mistakes come from missing a small detail like 'not' or 'except'.";
        }

        // Weak topics / performance
        if (matches(m, "weak", "performance", "progress", "where am i", "my topics", "struggling")) {
            if (weakTopics.isEmpty()) {
                return "🌟 You're doing excellent! You haven't dropped below 50% on any topics yet. Keep up the high standard!";
            }
            return "📊 Your analytics suggest focusing on:\n" +
                   weakTopics.stream().map(t -> "• **" + t + "**").collect(Collectors.joining("\n")) +
                   "\n\nWould you like a strategy tip for one of these?";
        }

        // Strategy tips / Shortcuts
        if (matches(m, "strategy", "shortcut", "trick", "faster", "speed")) {
            if (contextTopic != null) {
                return "⚡ **Shortcut for " + contextTopic + "**: " + getTopicStrategy(contextTopic) +
                       "\n\nThis is the fastest way to solve this type of question!";
            }

            String[][] shortcuts = {
                {"Percentage", "x% of y = y% of x"},
                {"Profit & Loss", "Find CP first using SP × 100/(100±P%)"},
                {"Time & Work", "Rate = 1/Time. For A and B together: (ab)/(a+b)"},
                {"Average", "Sum = Average × Number of terms"}
            };
            
            String reply;
            int attempts = 0;
            do {
                int idx = rand.nextInt(shortcuts.length);
                reply = "⚡ **Speed Tip (" + shortcuts[idx][0] + ")**: " + shortcuts[idx][1] + 
                        "\n\nWant another one? Just ask for 'more shortcuts'!";
                attempts++;
            } while (reply.equals(lastAssistantMsg) && attempts < 5);
            
            return reply;
        }

        // Topic-specific help
        for (String topic : TopicDistributionService.ALL_TOPICS) {
            if (m.contains(topic.toLowerCase())) {
                return getTopicStrategy(topic);
            }
        }

        // Motivation
        if (matches(m, "tired", "bored", "give up", "hard", "difficult")) {
            return "Aptitude training is a marathon, not a sprint. Take 5 minutes, grab some water, and come back. You've got this! 💪";
        }

        // Thanks
        if (matches(m, "thank", "thanks", "great", "awesome")) {
            return "Happy to help! Keep pushing your limits! 🚀";
        }

        // Jokes / Fun
        if (matches(m, "joke", "funny", "laugh")) {
            String[] jokes = {
                "Why was the math book sad? Because it had too many problems. 😄",
                "What is a mathematician's favorite dessert? Pi! 🥧",
                "Why did the student carry a ladder to the exam? Because they heard it was a high-level test! 🪜"
            };
            return jokes[rand.nextInt(jokes.length)];
        }

        // Identity / Purpose
        if (matches(m, "who are you", "what are you", "your name")) {
            return "I'm SkillForge AI, your dedicated aptitude and reasoning coach. I'm here to help you ace your competitive exams!";
        }

        // Help
        if (matches(m, "help", "what can you do", "commands")) {
            return "I can help you with:\n" +
                   "• **Step-by-step explanations** for questions\n" +
                   "• **Hints** when you're stuck\n" +
                   "• **Speed-solving shortcuts**\n" +
                   "• **Finding your weak topics**\n" +
                   "• **General study advice**\n\n" +
                   "Just ask!";
        }

        // Default
        return "I'm SkillForge AI, your personal tutor. I can help with:\n" +
               "• Explaining **aptitude questions**\n" +
               "• Giving **hints & shortcuts**\n" +
               "• Tracking your **weak topics**\n" +
               "• Answering **general study queries**\n\n" +
               "What can I do for you today?";
    }

    private String getTopicStrategy(String topic) {
        switch (topic) {
            case "Percentage": return "x% of y = (x * y) / 100. For successive changes: Net% = a + b + (ab/100).";
            case "Profit and Loss": return "Profit % = (SP - CP)/CP * 100. SP = CP * (100 + P%)/100. CP = SP * 100/(100 + P%).";
            case "Time and Work": return "If A can do work in 'n' days, A's 1 day work = 1/n. If A and B work together: 1/A + 1/B = 1/T.";
            case "Time Speed Distance": return "Distance = Speed × Time. Relative Speed: Same direction (S1-S2), Opposite (S1+S2). Avg Speed = 2xy/(x+y).";
            case "Ratio and Proportion": return "a : b = c : d ⇒ ad = bc. Duplicate Ratio of a:b is a²:b². Invertendo: b:a = d:c.";
            case "Simple and Compound Interest": return "SI = (P*R*T)/100. CI = P(1 + R/100)^T - P. Amount = P + Interest.";
            case "Averages": return "Average = (Sum of observations) / (Number of observations). New Average = (Old Sum + New Value) / (n + 1).";
            case "Mixtures and Allegations": return "(Cheaper Qty) / (Dearer Qty) = (Dearer Price - Mean Price) / (Mean Price - Cheaper Price).";
            case "Permutation": return "nPr = n! / (n - r)!. Used when order matters (e.g., arrangements).";
            case "Combination": return "nCr = n! / (r! * (n - r)!). Used when order doesn't matter (e.g., selection).";
            case "Probability": return "P(E) = (Favorable Outcomes) / (Total Outcomes). P(A or B) = P(A) + P(B) - P(A and B).";
            case "Number Systems": return "Dividend = (Divisor × Quotient) + Remainder. Sum of first n natural numbers = n(n+1)/2.";
            case "Logical Series": return "Identify the pattern: Arithmetic (+n), Geometric (×n), Squares/Cubes, or Alternating logic.";
            case "Coding Decoding": return "Map letters to positions (A=1, B=2...Z=26). Check for reverse mapping (A=26) or shifts (+n).";
            case "Blood Relations": return "Draw a family tree. Use '+' for male, '-' for female, and horizontal/vertical lines for generations.";
            case "Direction Sense": return "Always draw the N-S-E-W compass. Use Pythagoras theorem (a²+b²=c²) for displacement.";
            case "Data Interpretation": return "Focus on calculating percentage changes, ratios, and averages from the given tables or charts.";
            default: return "Identify what is given and what is asked. Match the variables to the standard formula for this topic.";
        }
    }

    private String extractTopicFromContext(Object ctx) {
        if (ctx == null) return null;
        String raw = ctx.toString();
        // Look for topic= or "topic":
        if (raw.contains("topic=") || raw.contains("\"topic\"")) {
            int s = raw.indexOf("topic=") >= 0 ? raw.indexOf("topic=") + 6 : raw.indexOf("\"topic\"") + 9;
            int e = raw.indexOf(",", s);
            if (e < 0) e = raw.indexOf("}", s);
            if (e > s) {
                String topic = raw.substring(s, e).replace("\"", "").replace("&", "and").trim();
                // 1. Exact match first
                for (String t : TopicDistributionService.ALL_TOPICS) {
                    if (topic.equalsIgnoreCase(t)) return t;
                }
                // 2. Fallback to start-of-word match if no exact match
                for (String t : TopicDistributionService.ALL_TOPICS) {
                    String firstWord = t.toLowerCase().split(" ")[0];
                    if (topic.toLowerCase().contains(firstWord)) {
                        return t;
                    }
                }
                return topic;
            }
        }
        return null;
    }

    private boolean matches(String message, String... keywords) {
        for (String kw : keywords) if (message.contains(kw)) return true;
        return false;
    }

    private String summariseContext(Object ctx) {
        if (ctx == null) return "no active question";
        String raw = ctx.toString();
        // Extract text field if it's a map
        if (raw.contains("text=") || raw.contains("\"text\"")) {
            int s = raw.indexOf("text=") >= 0 ? raw.indexOf("text=") + 5 : raw.indexOf("\"text\"") + 8;
            int e = raw.indexOf(",", s);
            if (e < 0) e = raw.indexOf("}", s);
            if (e > s) return raw.substring(s, Math.min(e, s + 200));
        }
        return raw.substring(0, Math.min(raw.length(), 200));
    }

    private void saveMessage(Long userId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        chatMessageRepository.save(msg);
    }
}
