package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.QuizSubmission;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByStudent_Id(Long studentId);

    List<QuizSubmission> findByQuiz_Id(Long quizId);

    Optional<QuizSubmission> findByStudent_IdAndQuiz_Id(Long studentId, Long quizId);

    boolean existsByStudent_IdAndQuiz_Id(Long studentId, Long quizId);
}
