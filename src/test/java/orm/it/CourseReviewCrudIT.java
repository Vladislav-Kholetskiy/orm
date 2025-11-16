package orm.it;

import orm.OrmApplication;
import orm.entity.Course;
import orm.entity.CourseReview;
import orm.entity.User;
import orm.model.Role;
import orm.repository.CourseRepository;
import orm.repository.CourseReviewRepository;
import orm.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrmApplication.class)
@Transactional
@Rollback
class CourseReviewCrudIT {

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("CRUD для отзывов на курс")
    void courseReviewCrudFlow() {
        // Студент
        User student = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .findFirst()
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Review Student");
                    u.setEmail("review.student@example.com");
                    u.setPassword("pwd");
                    u.setRole(Role.STUDENT);
                    u.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(u);
                });

        // Курс
        Course course = courseRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setTitle("Review course");
                    c.setDescription("Course for reviews");
                    c.setStatus(orm.model.CourseStatus.PUBLISHED);
                    c.setStartDate(LocalDate.now());
                    c.setEndDate(LocalDate.now().plusDays(10));
                    return courseRepository.save(c);
                });

        // CREATE
        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(5);
        review.setComment("Очень крутой курс!");
        review.setCreatedAt(LocalDateTime.now());

        CourseReview saved = courseReviewRepository.save(review);
        assertThat(saved.getId()).isNotNull();

        // READ
        CourseReview persisted = courseReviewRepository.findById(saved.getId()).orElseThrow();
        assertThat(persisted.getRating()).isEqualTo(5);
        assertThat(persisted.getComment()).contains("крутой");

        // UPDATE
        persisted.setRating(4);
        persisted.setComment("Курс хороший, но можно улучшить материалы.");
        courseReviewRepository.save(persisted);

        CourseReview updated = courseReviewRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getRating()).isEqualTo(4);
        assertThat(updated.getComment()).contains("улучшить");

        // DELETE
        courseReviewRepository.delete(updated);
        List<CourseReview> all = courseReviewRepository.findAll();
        assertThat(all.stream().noneMatch(r -> r.getId().equals(saved.getId()))).isTrue();
    }
}
