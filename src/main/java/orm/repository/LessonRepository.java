package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModule_IdOrderByOrderIndex(Long moduleId);
}

