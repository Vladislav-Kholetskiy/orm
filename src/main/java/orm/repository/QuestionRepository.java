package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuiz_Id(Long quizId);
}
