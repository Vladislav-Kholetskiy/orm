package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.AnswerOption;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestion_Id(Long questionId);
}
