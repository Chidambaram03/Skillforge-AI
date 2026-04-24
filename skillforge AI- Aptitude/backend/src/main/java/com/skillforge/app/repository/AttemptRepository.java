package com.skillforge.app.repository;

import com.skillforge.app.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserId(Long userId);

    @Query("SELECT a.question.id FROM Attempt a WHERE a.user.id = :userId")
    List<Long> findAttemptedQuestionIds(@Param("userId") Long userId);

    @Query(value = "SELECT q.question_text FROM attempts a JOIN questions q ON a.question_id = q.id WHERE a.user_id = :userId ORDER BY a.created_at DESC LIMIT :limit", nativeQuery = true)
    List<String> findRecentQuestionTexts(@Param("userId") Long userId, @Param("limit") int limit);
}
