package com.skillforge.app;

import com.skillforge.app.model.Question;
import com.skillforge.app.model.TopicVideo;
import com.skillforge.app.repository.QuestionRepository;
import com.skillforge.app.repository.TopicVideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TopicVideoRepository topicVideoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (questionRepository.count() == 0) {
            System.out.println("No questions found. Loading initial Aptitude sample data...");

            Question q1 = createQuestion(
                "Number Systems", "L1",
                "What is the sum of the first 10 positive integers?",
                "45", "50", "55", "60", "C",
                "Using the formula n(n+1)/2, where n=10. 10(11)/2 = 110/2 = 55."
            );

            Question q2 = createQuestion(
                "Blood Relations", "L2",
                "If A is the brother of B, B is the sister of C, and C is the father of D, how is D related to A?",
                "Brother", "Nephew/Niece", "Son", "Cannot be determined", "B",
                "A and C are siblings. Since C is the father of D, D is the child of A's sibling. Hence, D is the nephew or niece of A."
            );

            Question q3 = createQuestion(
                "Probability", "L3",
                "A bag contains 5 red and 3 green marbles. If two marbles are drawn at random without replacement, what is the probability that both are red?",
                "5/14", "25/64", "5/8", "10/28", "A",
                "P(First Red) = 5/8. P(Second Red | First Red) = 4/7. Total Probability = (5/8) * (4/7) = 20/56 = 5/14."
            );

            Question q4 = createQuestion(
                "Time and Work", "L4",
                "A can finish a work in 18 days and B can do the same work in half the time taken by A. Then, working together, what part of the same work they can finish in a day?",
                "1/6", "2/9", "3/18", "1/9", "A",
                "A's 1-day work = 1/18. B's 1-day work = 1/9 (since he takes 9 days). (A+B)'s 1-day work = 1/18 + 1/9 = 3/18 = 1/6."
            );

            Question q5 = createQuestion(
                "Data Interpretation", "L2",
                "A student scores 35, 40, and 45 in three exams. What score is needed in the fourth exam to average 42?",
                "45", "48", "50", "42", "B",
                "Total needed for average 42 = 42 * 4 = 168. Current total = 35 + 40 + 45 = 120. Required = 168 - 120 = 48."
            );

            Question q6 = createQuestion(
                "Logical Series", "L1",
                "Which word does NOT belong with the others?",
                "Leopard", "Cougar", "Elephant", "Lion", "C",
                "Leopard, Cougar, and Lion are all felines (cats). Elephant is a pachyderm."
            );

            Question q7 = createQuestion(
                "Number Systems", "L3",
                "What is the units digit of 7^105?",
                "1", "3", "7", "9", "C",
                "The cyclicity of 7 is 7, 9, 3, 1 (4 steps). 105 divided by 4 leaves a remainder of 1. So 7^1 = 7."
            );

            Question q8 = createQuestion(
                "Probability", "L2",
                "A card is drawn from a pack of 52. What is the probability that it is a king OR a spade?",
                "17/52", "16/52", "4/13", "1/4", "B",
                "Kings = 4, Spades = 13. One king is a spade (King of Spades). Total unique outcomes = 4 + 13 - 1 = 16. Probability = 16/52."
            );

            Question q9 = createQuestion(
                "Time and Work", "L3",
                "X can do a piece of work in 40 days. He works at it for 8 days and then Y finished it in 16 days. How long will X and Y take together to finish it?",
                "13 1/3 days", "15 days", "20 days", "25 days", "A",
                "X's 8 day work = 8/40 = 1/5. Remaining = 4/5. Y does 4/5 in 16 days, so full work in 20 days. Together 1-day = 1/40 + 1/20 = 3/40. Time = 40/3 = 13 1/3."
            );

            Question q10 = createQuestion(
                "Coding Decoding", "L4",
                "In a certain code language, COMPUTER is written as RFUVQNPC. How is MEDICINE written?",
                "EOJDJEFM", "EOJDEJFM", "MFEJDJOE", "MFEDJJOE", "B",
                "The word is reversed, then each letter is moved one step forward (except first and last which are swapped). Correct logic results in EOJDEJFM."
            );

            questionRepository.saveAll(Arrays.asList(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10));
            
            System.out.println("Sample Aptitude data loaded successfully.");
        }

        // Force re-seed for videos to ensure all 17 topics are updated correctly
        topicVideoRepository.deleteAll();
        System.out.println("Refreshing Topic Videos data...");

        topicVideoRepository.save(createVideo("Percentage", "https://www.youtube.com/embed/RWdNhJWwzSs", "Learn how to calculate percentages quickly."));
        topicVideoRepository.save(createVideo("Profit and Loss", "https://www.youtube.com/embed/T2odvmxqi1I", "Understanding Cost Price, Selling Price, and Profit/Loss."));
        topicVideoRepository.save(createVideo("Time and Work", "https://www.youtube.com/embed/KE7tQf9spPg", "Efficiency and time-based calculations for work done."));
        topicVideoRepository.save(createVideo("Time Speed Distance", "https://www.youtube.com/embed/KE7tQf9spPg", "Master concepts of relative speed and average speed."));
        topicVideoRepository.save(createVideo("Ratio and Proportion", "https://www.youtube.com/embed/jfoJBivWlnQ", "Simplifying ratios and using proportions in problems."));
        topicVideoRepository.save(createVideo("Simple and Compound Interest", "https://www.youtube.com/embed/jvRq87ZWzIk", "Formulae and tricks for Simple and Compound Interest."));
        topicVideoRepository.save(createVideo("Averages", "https://www.youtube.com/embed/rhSxQ4ieAYc", "Concepts of Average and its applications."));
        topicVideoRepository.save(createVideo("Mixtures and Allegations", "https://www.youtube.com/embed/OKSJDDAyqP0", "Solving mixture problems using allegation rule."));
        topicVideoRepository.save(createVideo("Permutation", "https://www.youtube.com/embed/ETiRE7N7pEI", "Understanding arrangements and Permutation formulas."));
        topicVideoRepository.save(createVideo("Combination", "https://www.youtube.com/embed/ETiRE7N7pEI", "Understanding selections and Combination formulas."));
        topicVideoRepository.save(createVideo("Probability", "https://www.youtube.com/embed/ximxxERGSUc", "Basic concepts and theorems of Probability."));
        topicVideoRepository.save(createVideo("Number Systems", "https://www.youtube.com/embed/qwHJtfEUCgE", "Introduction to Number Systems and Basic Concepts."));
        topicVideoRepository.save(createVideo("Logical Series", "https://www.youtube.com/embed/gXBuL_FyahE", "Identifying patterns in number and letter series."));
        topicVideoRepository.save(createVideo("Coding Decoding", "https://www.youtube.com/embed/wwN3mJ-b4FY", "Techniques to solve coding and decoding problems."));
        topicVideoRepository.save(createVideo("Blood Relations", "https://www.youtube.com/embed/LRdLhfDupMU", "Mapping family trees and solving relation puzzles."));
        topicVideoRepository.save(createVideo("Direction Sense", "https://www.youtube.com/embed/x0WkptLF6oE", "Logic for solving direction and distance based questions."));
        topicVideoRepository.save(createVideo("Data Interpretation", "https://www.youtube.com/embed/fA8cQW-nmIw", "Analyzing charts, tables, and graphs for information."));

        System.out.println("Topic Videos data refreshed successfully.");
    }

    private Question createQuestion(String topic, String level, String text, 
                                    String a, String b, String c, String d, 
                                    String correct, String explanation) {
        Question q = new Question();
        q.setTopic(topic);
        q.setDifficultyLevel(level);
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(correct);
        q.setExplanation(explanation);
        return q;
    }

    private TopicVideo createVideo(String topic, String url, String desc) {
        TopicVideo v = new TopicVideo();
        v.setTopic(topic.trim()); // Trim just in case
        v.setVideoUrl(url.trim()); // Trim to fix black screen issues
        v.setDescription(desc);
        return v;
    }
}
