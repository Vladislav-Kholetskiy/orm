package orm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import orm.entity.*;
import orm.entity.Module;
import orm.model.*;
import orm.repository.CategoryRepository;
import orm.repository.ResourceRepository;
import orm.repository.UserRepository;
import orm.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev") // будет запускаться только в профиле dev
@RequiredArgsConstructor
public class DataInitializer implements org.springframework.boot.CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ResourceRepository resourceRepository;

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;

    @Override
    @Transactional
    public void run(String... args) {
        // Чтобы не создавать данные каждый запуск, если уже есть пользователи — выходим
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Пользователи
        User teacher = User.builder()
                .name("John Teacher")
                .email("teacher@example.com")
                .password("password") // в учебном проекте ок
                .role(Role.TEACHER)
                .build();

        User student1 = User.builder()
                .name("Alice Student")
                .email("alice@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        User student2 = User.builder()
                .name("Bob Student")
                .email("bob@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        userRepository.saveAll(List.of(teacher, student1, student2));

        // 2. Категория
        Category category = Category.builder()
                .name("Programming")
                .description("Programming courses")
                .build();
        categoryRepository.save(category);

        // 3. Курс
        Course course = Course.builder()
                .title("Spring & JPA Basics")
                .description("Introductory course to Spring Boot and JPA")
                .duration(40)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(CourseStatus.PUBLISHED)
                .build();

        Course savedCourse = courseService.createCourse(
                course,
                teacher.getId(),
                category.getId()
        );

        // 4. Модуль
        Module module1 = Module.builder()
                .title("Module 1: JPA Fundamentals")
                .description("Basic concepts of JPA and Hibernate")
                .orderIndex(1)
                .build();

        Module savedModule = courseService.addModuleToCourse(savedCourse.getId(), module1);

        // 5. Урок
        Lesson lesson1 = Lesson.builder()
                .title("Lesson 1: Entities and Relationships")
                .content("In this lesson, we talk about entities, relationships, and mappings in JPA.")
                .videoUrl("https://example.com/videos/jpa-entities")
                .orderIndex(1)
                .build();

        Lesson savedLesson = courseService.addLessonToModule(savedModule.getId(), lesson1);

        // 6. Ресурс к уроку
        Resource resource = Resource.builder()
                .type(ResourceType.LINK)
                .url("https://docs.spring.io/spring-data/jpa/docs/current/reference/html/")
                .description("Spring Data JPA reference documentation")
                .lesson(savedLesson)
                .build();
        resourceRepository.save(resource);

        // 7. Задание
        Assignment assignment = Assignment.builder()
                .title("Homework 1: Design JPA Model")
                .description("Design a JPA model with at least 5 entities and proper relationships.")
                .dueDate(LocalDateTime.now().plusDays(7))
                .maxScore(100)
                .build();

        Assignment savedAssignment = assignmentService.createAssignment(savedLesson.getId(), assignment);

        // 8. Запись студентов на курс
        enrollmentService.enrollStudent(savedCourse.getId(), student1.getId());
        enrollmentService.enrollStudent(savedCourse.getId(), student2.getId());

        // 9. Отправка решения по заданию
        Submission submission1 = submissionService.submitAssignment(
                student1.getId(),
                savedAssignment.getId(),
                "My JPA model design as requested in the assignment."
        );

        submissionService.gradeSubmission(
                submission1.getId(),
                95,
                "Very good job! Only minor improvements needed."
        );

        // 10. Тест (Quiz) для модуля
        Quiz quiz = Quiz.builder()
                .title("Quiz 1: JPA Basics")
                .timeLimitMinutes(15)
                .build();

        // Первый вопрос
        Question q1 = Question.builder()
                .text("What does JPA stand for?")
                .type(QuestionType.SINGLE_CHOICE)
                .build();

        AnswerOption q1o1 = AnswerOption.builder()
                .text("Java Persistence API")
                .isCorrect(true)
                .build();

        AnswerOption q1o2 = AnswerOption.builder()
                .text("Java Printing Application")
                .isCorrect(false)
                .build();

        q1.getOptions().add(q1o1);
        q1.getOptions().add(q1o2);
        q1o1.setQuestion(q1);
        q1o2.setQuestion(q1);

        // Второй вопрос
        Question q2 = Question.builder()
                .text("Which annotation is used to map a class as an entity?")
                .type(QuestionType.SINGLE_CHOICE)
                .build();

        AnswerOption q2o1 = AnswerOption.builder()
                .text("@Table")
                .isCorrect(false)
                .build();

        AnswerOption q2o2 = AnswerOption.builder()
                .text("@Entity")
                .isCorrect(true)
                .build();

        q2.getOptions().add(q2o1);
        q2.getOptions().add(q2o2);
        q2o1.setQuestion(q2);
        q2o2.setQuestion(q2);

        quiz.getQuestions().add(q1);
        quiz.getQuestions().add(q2);
        q1.setQuiz(quiz);
        q2.setQuiz(quiz);

        Quiz savedQuiz = quizService.createQuizForModule(savedModule.getId(), quiz);

        // 11. Прохождение теста student1 с правильными ответами
        // После сохранения у вопросов и опций уже будут ID
        Question savedQ1 = savedQuiz.getQuestions().get(0);
        Question savedQ2 = savedQuiz.getQuestions().get(1);

        Long q1CorrectOptionId = savedQ1.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .findFirst()
                .orElseThrow()
                .getId();

        Long q2CorrectOptionId = savedQ2.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .findFirst()
                .orElseThrow()
                .getId();

        Map<Long, List<Long>> answers = Map.of(
                savedQ1.getId(), List.of(q1CorrectOptionId),
                savedQ2.getId(), List.of(q2CorrectOptionId)
        );

        quizService.takeQuiz(student1.getId(), savedQuiz.getId(), answers);
    }
}
