package orm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orm.entity.Course;
import orm.entity.Enrollment;
import orm.entity.User;
import orm.model.EnrollmentStatus;
import orm.model.Role;
import orm.repository.CourseRepository;
import orm.repository.EnrollmentRepository;
import orm.repository.UserRepository;
import orm.service.EnrollmentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public Enrollment enrollStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalStateException("User is not a student: " + studentId);
        }

        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId)) {
            throw new IllegalStateException("Student is already enrolled in the course");
        }

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(student)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }


    @Override
    public void unenrollStudent(Long courseId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollmentRepository.delete(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        return enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesForStudent(Long studentId) {
        return enrollmentRepository.findByStudent_Id(studentId).stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getStudentsForCourse(Long courseId) {
        return enrollmentRepository.findByCourse_Id(courseId).stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toList());
    }
}
