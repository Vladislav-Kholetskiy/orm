package orm.service;

import orm.entity.Course;
import orm.entity.Enrollment;
import orm.entity.User;

import java.util.List;

public interface EnrollmentService {

    Enrollment enrollStudent(Long courseId, Long studentId);

    void unenrollStudent(Long courseId, Long studentId);

    void cancelEnrollment(Long enrollmentId);

    boolean isStudentEnrolled(Long courseId, Long studentId);

    List<Course> getCoursesForStudent(Long studentId);

    List<User> getStudentsForCourse(Long courseId);
}
