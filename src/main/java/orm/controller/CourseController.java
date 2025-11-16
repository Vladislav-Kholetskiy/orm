package orm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import orm.entity.Course;
import orm.entity.Enrollment;
import orm.entity.Lesson;
import orm.entity.Module;
import orm.entity.User;
import orm.model.CourseStatus;
import orm.service.CourseService;
import orm.service.EnrollmentService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .duration(request.duration())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status() != null ? request.status() : CourseStatus.DRAFT)
                .build();

        Course created = courseService.createCourse(
                course,
                request.teacherId(),
                request.categoryId()
        );

        return ResponseEntity
                .created(URI.create("/api/courses/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id) {
        return courseService.getCourseWithStructure(id);
    }

    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id,
                               @Valid @RequestBody CourseUpdateRequest request) {
        Course updatedData = Course.builder()
                .title(request.title())
                .description(request.description())
                .duration(request.duration())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status())
                .build();

        return courseService.updateCourse(id, updatedData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Course> getCoursesByTeacher(@PathVariable Long teacherId) {
        return courseService.getCoursesByTeacher(teacherId);
    }

    @GetMapping("/category/{categoryId}")
    public List<Course> getCoursesByCategory(@PathVariable Long categoryId) {
        return courseService.getCoursesByCategory(categoryId);
    }

    // --- Модули и уроки ---

    @PostMapping("/{courseId}/modules")
    public Module addModule(@PathVariable Long courseId,
                            @Valid @RequestBody ModuleCreateRequest request) {
        Module module = Module.builder()
                .title(request.title())
                .description(request.description())
                .orderIndex(request.orderIndex())
                .build();
        return courseService.addModuleToCourse(courseId, module);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public Lesson addLesson(@PathVariable Long moduleId,
                            @Valid @RequestBody LessonCreateRequest request) {
        Lesson lesson = Lesson.builder()
                .title(request.title())
                .content(request.content())
                .videoUrl(request.videoUrl())
                .orderIndex(request.orderIndex())
                .build();
        return courseService.addLessonToModule(moduleId, lesson);
    }

    // --- Запись на курс ---

    @PostMapping("/{courseId}/enroll")
    public Enrollment enrollStudent(@PathVariable Long courseId,
                                    @RequestParam @NotNull(message = "studentId is required") Long studentId) {
        return enrollmentService.enrollStudent(courseId, studentId);
    }

    @DeleteMapping("/{courseId}/enroll")
    public ResponseEntity<Void> unenrollStudent(@PathVariable Long courseId,
                                                @RequestParam @NotNull(message = "studentId is required") Long studentId) {
        enrollmentService.unenrollStudent(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/students")
    public List<User> getStudentsForCourse(@PathVariable Long courseId) {
        return enrollmentService.getStudentsForCourse(courseId);
    }

    // --- DTO ---

    public record CourseCreateRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must be at most 255 characters")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            @NotNull(message = "Duration is required")
            @Positive(message = "Duration must be positive")
            Integer duration,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            LocalDate endDate,

            CourseStatus status,

            @NotNull(message = "Teacher ID is required")
            Long teacherId,

            @NotNull(message = "Category ID is required")
            Long categoryId
    ) {
    }

    public record CourseUpdateRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 255, message = "Title must be at most 255 characters")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            @NotNull(message = "Duration is required")
            @Positive(message = "Duration must be positive")
            Integer duration,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            LocalDate endDate,

            CourseStatus status
    ) {
    }

    public record ModuleCreateRequest(
            @NotBlank(message = "Title is required")
            String title,

            @Size(max = 2000, message = "Description must be at most 2000 characters")
            String description,

            @NotNull(message = "Order index is required")
            @Min(value = 0, message = "Order index must be >= 0")
            Integer orderIndex
    ) {
    }

    public record LessonCreateRequest(
            @NotBlank(message = "Title is required")
            String title,

            @NotBlank(message = "Content is required")
            String content,

            String videoUrl,

            @NotNull(message = "Order index is required")
            @Min(value = 0, message = "Order index must be >= 0")
            Integer orderIndex
    ) {
    }
}

