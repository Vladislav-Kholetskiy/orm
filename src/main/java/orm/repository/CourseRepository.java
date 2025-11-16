package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategory_Id(Long categoryId);

    List<Course> findByTeacher_Id(Long teacherId);
}

