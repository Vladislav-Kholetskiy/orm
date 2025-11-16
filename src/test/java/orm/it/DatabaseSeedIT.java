package orm.it;

import orm.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev") // ВАЖНО: включаем тот же профиль, где работает сидер
class DatabaseSeedIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerOptionRepository answerOptionRepository;
    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;
    @Autowired
    private SubmissionRepository submissionRepository;

    @Test
    void demoDataIsLoadedOnStartup() {
        long userCount = userRepository.count();
        assertThat(userCount)
                .as("Должно быть минимум 3 пользователя (1 teacher + 2 students)")
                .isGreaterThanOrEqualTo(3);

        assertThat(courseRepository.count())
                .as("Должен быть хотя бы один курс")
                .isGreaterThanOrEqualTo(1);

        assertThat(moduleRepository.count())
                .as("Должен быть хотя бы один модуль")
                .isGreaterThanOrEqualTo(1);

        assertThat(lessonRepository.count())
                .as("Должен быть хотя бы один урок")
                .isGreaterThanOrEqualTo(1);

        assertThat(assignmentRepository.count())
                .as("Должно быть хотя бы одно задание")
                .isGreaterThanOrEqualTo(1);

        assertThat(enrollmentRepository.count())
                .as("Должна быть хотя бы одна запись на курс")
                .isGreaterThanOrEqualTo(1);

        assertThat(quizRepository.count())
                .as("Должен быть хотя бы один тест")
                .isGreaterThanOrEqualTo(1);

        assertThat(questionRepository.count())
                .as("Должен быть хотя бы один вопрос теста")
                .isGreaterThanOrEqualTo(1);

        assertThat(answerOptionRepository.count())
                .as("Должно быть хотя бы два варианта ответов")
                .isGreaterThanOrEqualTo(2);

        assertThat(quizSubmissionRepository.count())
                .as("Должен быть хотя бы один результат теста")
                .isGreaterThanOrEqualTo(1);

        assertThat(submissionRepository.count())
                .as("Должно быть хотя бы одно решение задания")
                .isGreaterThanOrEqualTo(1);
    }
}
