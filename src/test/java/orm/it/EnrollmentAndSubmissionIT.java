package orm.it;

import orm.entity.Assignment;
import orm.entity.Category;
import orm.entity.Course;
import orm.entity.Enrollment;
import orm.entity.Lesson;
import orm.entity.Submission;
import orm.entity.User;
import orm.model.CourseStatus;
import orm.model.EnrollmentStatus;
import orm.model.Role;
import orm.model.SubmissionStatus;
import orm.repository.AssignmentRepository;
import orm.repository.CategoryRepository;
import orm.repository.CourseRepository;
import orm.repository.EnrollmentRepository;
import orm.repository.LessonRepository;
import orm.repository.SubmissionRepository;
import orm.repository.ModuleRepository;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentAndSubmissionCrudIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    /**
     * Полный сценарий:
     *  - создаём курс и студента
     *  - записываем студента на курс (Enrollment)
     *  - создаём урок и задание
     *  - студент отправляет решение (Submission)
     */
    @Test
    @Transactional
    void fullEnrollmentAndSubmissionFlow() {
        // Преподаватель
        User teacher = userRepository.save(
                User.builder()
                        .name("Teacher Flow")
                        .email("teacher.flow@example.com")
                        .password("secret")
                        .role(Role.TEACHER)
                        .build()
        );

        // Студент
        User student = userRepository.save(
                User.builder()
                        .name("Student Flow")
                        .email("student.flow@example.com")
                        .password("secret")
                        .role(Role.STUDENT)
                        .build()
        );

        // Категория
        Category category = categoryRepository.save(
                Category.builder()
                        .name("ORM")
                        .description("ORM courses")
                        .build()
        );

        // Курс
        Course course = courseRepository.save(
                Course.builder()
                        .title("ORM Deep Dive")
                        .description("Course for flow test")
                        .status(CourseStatus.PUBLISHED)
                        .teacher(teacher)
                        .category(category)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(10))
                        .build()
        );

        // Запись студента на курс
        Enrollment enrollment = enrollmentRepository.save(
                Enrollment.builder()
                        .student(student)
                        .course(course)
                        .status(EnrollmentStatus.ACTIVE)
                        .enrolledAt(LocalDateTime.now())
                        .build()
        );

        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

        // Модуль и урок
        orm.entity.Module module = moduleRepository.save(
                orm.entity.Module.builder()
                        .title("Module A")
                        .description("Module for assignment")
                        .orderIndex(1)
                        .course(course)
                        .build()
        );

        Lesson lesson = lessonRepository.save(
                Lesson.builder()
                        .title("Lesson A1")
                        .orderIndex(1)
                        .content("Assignment lesson")
                        .module(module)
                        .build()
        );

        // Задание
        Assignment assignment = assignmentRepository.save(
                Assignment.builder()
                        .title("Homework 1")
                        .description("Do something important")
                        .dueDate(LocalDateTime.now().plusDays(3))
                        .maxScore(100)
                        .lesson(lesson)
                        .build()
        );

        // Решение студента
        Submission submission = submissionRepository.save(
                Submission.builder()
                        .assignment(assignment)
                        .student(student)
                        .submittedAt(LocalDateTime.now())
                        .content("My awesome solution")
                        .status(SubmissionStatus.SUBMITTED)
                        .build()
        );

        assertThat(submission.getId()).isNotNull();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);

        // Обновляем оценку и статус
        submission.setScore(95);
        submission.setStatus(SubmissionStatus.ACCEPTED);
        submission.setFeedback("Well done!");

        Submission graded = submissionRepository.save(submission);

        assertThat(graded.getScore()).isEqualTo(95);
        assertThat(graded.getStatus()).isEqualTo(SubmissionStatus.ACCEPTED);
        assertThat(graded.getFeedback()).isEqualTo("Well done!");

        // Проверка выборки
        List<Submission> byAssignment = submissionRepository.findAll();
        assertThat(byAssignment).isNotEmpty();
    }
}
