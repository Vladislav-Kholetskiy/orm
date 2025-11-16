package orm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import orm.entity.Course;
import orm.entity.User;
import orm.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/students/{studentId}/courses")
    public List<Course> getCoursesForStudent(@PathVariable Long studentId) {
        return enrollmentService.getCoursesForStudent(studentId);
    }

    @GetMapping("/courses/{courseId}/students")
    public List<User> getStudentsForCourse(@PathVariable Long courseId) {
        return enrollmentService.getStudentsForCourse(courseId);
    }

    @GetMapping("/courses/{courseId}/students/{studentId}")
    public boolean isStudentEnrolled(@PathVariable Long courseId,
                                     @PathVariable Long studentId) {
        return enrollmentService.isStudentEnrolled(courseId, studentId);
    }
}

