package orm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import orm.entity.CourseReview;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    List<CourseReview> findByCourse_Id(Long courseId);

    List<CourseReview> findByStudent_Id(Long studentId);
}

