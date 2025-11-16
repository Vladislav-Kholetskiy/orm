package orm.it;

import orm.entity.Category;
import orm.entity.Course;
import orm.entity.Lesson;
import orm.entity.User;
import orm.model.CourseStatus;
import orm.model.Role;
import orm.repository.CategoryRepository;
import orm.repository.CourseRepository;
import orm.repository.LessonRepository;
import orm.repository.ModuleRepository;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // тесты работают на H2 / test-профиль
class CourseCrudIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Test
    @Transactional
    void createCourseWithModuleAndLesson() {
        // 1) Преподаватель
        User teacher = User.builder()
                .name("Test Teacher")
                .email("teacher@test.com")
                .password("secret")
                .role(Role.TEACHER)
                .build();
        teacher = userRepository.save(teacher);

        // 2) Категория
        Category category = Category.builder()
                .name("Programming")
                .description("Programming courses")
                .build();
        category = categoryRepository.save(category);

        // 3) Курс
        Course course = Course.builder()
                .title("Hibernate Basics")
                .description("Intro to Hibernate")
                .status(CourseStatus.PUBLISHED)
                .duration(30)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .teacher(teacher)
                .category(category)
                .build();
        course = courseRepository.save(course);

        // 4) Модуль, привязанный к курсу
        orm.entity.Module module = orm.entity.Module.builder()
                .title("Introduction")
                .description("Intro module")
                .orderIndex(1)
                .course(course)
                .build();
        module = moduleRepository.save(module);

        // 5) Урок, привязанный к модулю
        Lesson lesson = Lesson.builder()
                .title("What is ORM")
                .orderIndex(1)
                .module(module)
                .videoUrl("https://example.com/orm")
                .content("ORM basics") // <-- String, а не byte[]
                .build();
        lesson = lessonRepository.save(lesson);

        // 6) Проверки

        // 6.1 Курс найден и связан с учителем и категорией
        Course foundCourse = courseRepository.findById(course.getId())
                .orElseThrow(() -> new IllegalStateException("Course not found"));
        assertThat(foundCourse.getTitle()).isEqualTo("Hibernate Basics");
        assertThat(foundCourse.getTeacher().getId()).isEqualTo(teacher.getId());
        assertThat(foundCourse.getCategory().getId()).isEqualTo(category.getId());

        // 6.2 Модуль найден по course_id
        List<orm.entity.Module> modules = moduleRepository.findByCourseId(course.getId());
        assertThat(modules)
                .as("Должен быть 1 модуль у курса")
                .hasSize(1);

        orm.entity.Module foundModule = modules.get(0);
        assertThat(foundModule.getTitle()).isEqualTo("Introduction");

        // 6.3 Уроки найдены по module_id с сортировкой
        List<Lesson> lessons = lessonRepository.findByModule_IdOrderByOrderIndex(foundModule.getId());
        assertThat(lessons)
                .as("Должен быть 1 урок в модуле")
                .hasSize(1);

        Lesson foundLesson = lessons.get(0);
        assertThat(foundLesson.getTitle()).isEqualTo("What is ORM");
    }
}
