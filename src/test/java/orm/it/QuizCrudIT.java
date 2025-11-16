package orm.it;

import orm.OrmApplication;
import orm.entity.AnswerOption;
import orm.entity.Course;
import orm.entity.Question;
import orm.entity.Quiz;
import orm.entity.QuizSubmission;
import orm.entity.User;
import orm.model.QuestionType;
import orm.model.Role;
import orm.model.CourseStatus;
import orm.repository.AnswerOptionRepository;
import orm.repository.CourseRepository;
import orm.repository.QuestionRepository;
import orm.repository.QuizRepository;
import orm.repository.QuizSubmissionRepository;
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
class QuizCrudIT {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("Создание квиза, вопросов и вариантов ответов")
    void createQuizWithQuestions() {

        // -- teacher (final)
        final User teacher = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEACHER)
                .findFirst()
                .orElseGet(() -> {
                    User t = new User();
                    t.setName("Teacher QZ");
                    t.setEmail("teacher.qz@example.com");
                    t.setPassword("pwd");
                    t.setRole(Role.TEACHER);
                    t.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(t);
                });

        // -- create course (final)
        Course c = new Course();
        c.setTitle("Java QZ course");
        c.setDescription("Quiz test course");
        c.setStatus(CourseStatus.DRAFT);
        c.setTeacher(teacher);
        c.setStartDate(LocalDate.now());
        c.setEndDate(LocalDate.now().plusDays(30));

        final Course course = courseRepository.save(c);

        // -- quiz (final)
        Quiz qz = new Quiz();
        qz.setTitle("Java Basics Quiz");
        qz.setTimeLimitMinutes(12);
        final Quiz quiz = quizRepository.save(qz);

        // ===== QUESTION 1 =====
        Question q1 = new Question();
        q1.setQuiz(quiz);
        q1.setText("Что такое JVM?");
        q1.setType(QuestionType.SINGLE_CHOICE);
        final Question question1 = questionRepository.save(q1);

        AnswerOption q1o1 = new AnswerOption();
        q1o1.setQuestion(question1);
        q1o1.setText("Виртуальная машина Java");
        q1o1.setCorrect(true);
        answerOptionRepository.save(q1o1);

        AnswerOption q1o2 = new AnswerOption();
        q1o2.setQuestion(question1);
        q1o2.setText("База данных");
        q1o2.setCorrect(false);
        answerOptionRepository.save(q1o2);

        // ===== QUESTION 2 =====
        Question q2 = new Question();
        q2.setQuiz(quiz);
        q2.setText("Выберите примитивы");
        q2.setType(QuestionType.MULTIPLE_CHOICE);
        final Question question2 = questionRepository.save(q2);

        AnswerOption q2o1 = new AnswerOption();
        q2o1.setQuestion(question2);
        q2o1.setText("int");
        q2o1.setCorrect(true);
        answerOptionRepository.save(q2o1);

        AnswerOption q2o2 = new AnswerOption();
        q2o2.setQuestion(question2);
        q2o2.setText("String");
        q2o2.setCorrect(false);
        answerOptionRepository.save(q2o2);

        // ===== CHECKS =====

        Quiz persisted = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(persisted.getTitle()).isEqualTo("Java Basics Quiz");

        List<Question> allQuestions = questionRepository.findAll();
        assertThat(allQuestions)
                .filteredOn(q -> q.getQuiz() != null && q.getQuiz().getId().equals(quiz.getId()))
                .hasSize(2);

        List<AnswerOption> q1Options = answerOptionRepository.findAll().stream()
                .filter(o -> o.getQuestion().getId().equals(question1.getId()))
                .toList();
        assertThat(q1Options).hasSize(2);

        List<AnswerOption> q2Options = answerOptionRepository.findAll().stream()
                .filter(o -> o.getQuestion().getId().equals(question2.getId()))
                .toList();
        assertThat(q2Options).hasSize(2);
    }

    @Test
    @DisplayName("Создание QuizSubmission")
    void createQuizSubmission() {

        // -- student (final)
        final User student = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .findFirst()
                .orElseGet(() -> {
                    User s = new User();
                    s.setName("Quiz Student");
                    s.setEmail("quiz.student@example.com");
                    s.setPassword("pwd");
                    s.setRole(Role.STUDENT);
                    s.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(s);
                });

        // -- quiz (final)
        Quiz q = new Quiz();
        q.setTitle("Test Quiz");
        q.setTimeLimitMinutes(5);
        final Quiz quiz = quizRepository.save(q);

        // -- submission
        QuizSubmission sub = new QuizSubmission();
        sub.setQuiz(quiz);
        sub.setStudent(student);
        sub.setPassed(true);
        sub.setScore(85);
        sub.setTakenAt(LocalDateTime.now());

        QuizSubmission saved = quizSubmissionRepository.save(sub);

        QuizSubmission real = quizSubmissionRepository.findById(saved.getId()).orElseThrow();

        assertThat(real.getQuiz().getId()).isEqualTo(quiz.getId());
        assertThat(real.getStudent().getId()).isEqualTo(student.getId());
        assertThat(real.getScore()).isEqualTo(85);
        assertThat(real.isPassed()).isTrue();
    }
}
