package orm.service;

import orm.entity.Category;
import orm.entity.Course;
import orm.entity.User;
import orm.model.CourseStatus;
import orm.model.Role;
import orm.repository.CategoryRepository;
import orm.repository.CourseRepository;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import orm.service.impl.CourseServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void createCourse_setsTeacherCategoryAndDraftStatus() {
        // given
        Long teacherId = 1L;
        Long categoryId = 10L;

        User teacher = User.builder()
                .id(teacherId)
                .name("Teacher One")
                .email("teacher@example.com")
                .password("pwd")
                .role(Role.TEACHER) // роль сейчас логика не использует, но пусть будет
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("Programming")
                .build();

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        Course newCourse = Course.builder()
                .title("Java Basics")
                .description("Intro to Java")
                .duration(40)
                // статус не задаём, чтобы сервис сам поставил DRAFT
                .build();

        // when
        Course created = courseService.createCourse(newCourse, teacherId, categoryId);

        // then
        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getTitle()).isEqualTo("Java Basics");
        assertThat(created.getTeacher()).isEqualTo(teacher);
        assertThat(created.getCategory()).isEqualTo(category);
        assertThat(created.getStatus()).isEqualTo(CourseStatus.DRAFT);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository, times(1)).save(captor.capture());

        Course saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Java Basics");
        assertThat(saved.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(saved.getTeacher()).isEqualTo(teacher);
        assertThat(saved.getCategory()).isEqualTo(category);
    }

    @Test
    void createCourse_throwsIfTeacherNotFound() {
        Long teacherId = 2L;
        Long categoryId = 10L;

        // учитель не найден
        when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

        Course newCourse = Course.builder()
                .title("Java")
                .description("desc")
                .duration(10)
                .build();

        assertThatThrownBy(() ->
                courseService.createCourse(newCourse, teacherId, categoryId)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Teacher not found");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_changesStatusToPublished() {
        Long courseId = 5L;

        Course course = Course.builder()
                .id(courseId)
                .title("Java")
                .status(CourseStatus.DRAFT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course published = courseService.publishCourse(courseId);

        assertThat(published.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        verify(courseRepository, times(1)).save(course);
    }
}
