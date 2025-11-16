package orm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orm.entity.Category;
import orm.entity.Course;
import orm.entity.Lesson;
import orm.entity.Module;
import orm.entity.User;
import orm.model.CourseStatus;
import orm.repository.CategoryRepository;
import orm.repository.CourseRepository;
import orm.repository.LessonRepository;
import orm.repository.ModuleRepository;
import orm.repository.UserRepository;
import orm.service.CourseService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    @Override
    public Course createCourse(Course course, Long teacherId, Long categoryId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        course.setTeacher(teacher);
        course.setCategory(category);

        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.DRAFT);
        }

        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long courseId, Course updatedData) {
        Course existing = getCourse(courseId);

        existing.setTitle(updatedData.getTitle());
        existing.setDescription(updatedData.getDescription());
        existing.setDuration(updatedData.getDuration());
        existing.setStartDate(updatedData.getStartDate());
        existing.setEndDate(updatedData.getEndDate());
        if (updatedData.getStatus() != null) {
            existing.setStatus(updatedData.getStatus());
        }

        return courseRepository.save(existing);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = getCourse(courseId);
        courseRepository.delete(course);
    }

    @Override
    public Course publishCourse(Long courseId) {
        Course course = getCourse(courseId);
        course.setStatus(CourseStatus.PUBLISHED);
        return courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseWithStructure(Long courseId) {
        // Простой вариант: вернём курс, а модули/уроки загрузятся лениво в транзакции.
        Course course = getCourse(courseId);
        course.getModules().forEach(module -> module.getLessons().size());
        return course;
    }

    @Override
    public Module addModuleToCourse(Long courseId, Module module) {
        Course course = getCourse(courseId);
        module.setCourse(course);
        moduleRepository.save(module);
        course.getModules().add(module);
        return module;
    }

    @Override
    public Lesson addLessonToModule(Long moduleId, Lesson lesson) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
        lesson.setModule(module);
        lessonRepository.save(lesson);
        module.getLessons().add(lesson);
        return lesson;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacher_Id(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategory_Id(categoryId);
    }
}
