package com.skillforge.app.repository;

import com.skillforge.app.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    // Custom query to fetch a random question for a specific level that the user hasn't attempted recently
    @Query(value = "SELECT * FROM questions q WHERE q.difficulty_level = :level ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomQuestionByDifficultyLevel(@Param("level") String level);

    @Query(value = "SELECT * FROM questions q WHERE q.difficulty_level = :level AND q.id NOT IN (:answeredIds) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomQuestionExclude(@Param("level") String level, @Param("answeredIds") List<Long> answeredIds);
    
    List<Question> findByTopicAndDifficultyLevel(String topic, String difficultyLevel);
}
