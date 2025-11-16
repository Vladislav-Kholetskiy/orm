package orm.service;

import orm.entity.Course;
import orm.entity.Enrollment;
import orm.entity.User;
import orm.model.EnrollmentStatus;
import orm.model.Role;
import orm.repository.CourseRepository;
import orm.repository.EnrollmentRepository;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import orm.service.impl.EnrollmentServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void enrollStudent_createsEnrollmentIfNotExists() {
        Long courseId = 1L;
        Long studentId = 2L;

        User student = User.builder()
                .id(studentId)
                .name("Student")
                .email("student@example.com")
                .password("pwd")
                .role(Role.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .title("Java")
                .build();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId)).thenReturn(false);

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });

        Enrollment enrollment = enrollmentService.enrollStudent(courseId, studentId);

        assertThat(enrollment.getId()).isEqualTo(100L);
        assertThat(enrollment.getCourse()).isEqualTo(course);
        assertThat(enrollment.getStudent()).isEqualTo(student);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_throwsIfAlreadyEnrolled() {
        Long courseId = 1L;
        Long studentId = 2L;

        Course course = Course.builder()
                .id(courseId)
                .title("Java")
                .build();

        User student = User.builder()
                .id(studentId)
                .name("Student")
                .role(Role.STUDENT)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        // ВАЖНО: порядок аргументов как в сервисе: (studentId, courseId)
        when(enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                enrollmentService.enrollStudent(courseId, studentId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Student is already enrolled in the course");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_throwsIfUserIsNotStudent() {
        Long courseId = 1L;
        Long userId = 2L;

        Course course = Course.builder()
                .id(courseId)
                .title("Java")
                .build();

        User notStudent = User.builder()
                .id(userId)
                .name("Teacher")
                .role(Role.TEACHER)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(notStudent));

        assertThatThrownBy(() ->
                enrollmentService.enrollStudent(courseId, userId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is not a student");

        verify(enrollmentRepository, never()).existsByStudent_IdAndCourse_Id(anyLong(), anyLong());
        verify(enrollmentRepository, never()).save(any());
    }


    @Test
    void cancelEnrollment_setsStatusCancelled() {
        Long enrollmentId = 10L;

        Enrollment enrollment = Enrollment.builder()
                .id(enrollmentId)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        enrollmentService.cancelEnrollment(enrollmentId);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(enrollmentRepository, times(1)).save(enrollment);
    }
}
