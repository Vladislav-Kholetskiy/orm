package orm.service;

import orm.entity.Course;
import orm.entity.Lesson;
import orm.entity.Module;

import java.util.List;

public interface CourseService {

    Course createCourse(Course course, Long teacherId, Long categoryId);

    Course updateCourse(Long courseId, Course updatedData);

    void deleteCourse(Long courseId);

    Course getCourse(Long courseId);

    Course publishCourse(Long courseId);

    Course getCourseWithStructure(Long courseId);

    Module addModuleToCourse(Long courseId, Module module);

    Lesson addLessonToModule(Long moduleId, Lesson lesson);

    List<Course> getCoursesByTeacher(Long teacherId);

    List<Course> getCoursesByCategory(Long categoryId);
}

