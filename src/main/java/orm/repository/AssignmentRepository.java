package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Assignment;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLesson_Id(Long lessonId);
}

