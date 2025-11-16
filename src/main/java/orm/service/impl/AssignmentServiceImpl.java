package orm.service.impl;

import lombok.RequiredArgsConstructor;
import orm.entity.Assignment;
import orm.entity.Lesson;
import orm.entity.Submission;
import orm.entity.User;
import orm.model.SubmissionStatus;
import orm.repository.AssignmentRepository;
import orm.repository.LessonRepository;
import orm.repository.SubmissionRepository;
import orm.repository.UserRepository;
import orm.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Override
    public Assignment createAssignment(Long lessonId, Assignment assignment) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Урок не найден: " + lessonId));
        assignment.setLesson(lesson);
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, Assignment updatedData) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Задание не найдено: " + assignmentId));

        assignment.setTitle(updatedData.getTitle());
        assignment.setDescription(updatedData.getDescription());
        assignment.setDueDate(updatedData.getDueDate());
        assignment.setMaxScore(updatedData.getMaxScore());

        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new IllegalArgumentException("Задание не найдено: " + assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Задание не найдено: " + assignmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByLesson(Long lessonId) {
        return assignmentRepository.findByLesson_Id(lessonId);
    }

    @Override
    public Submission submitAssignment(Long assignmentId, Long studentId, String content) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Задание не найдено: " + assignmentId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + studentId));

        // предотвращаем повторную отправку
        boolean alreadyExists = submissionRepository
                .existsByAssignment_IdAndStudent_Id(assignmentId, studentId);
        if (alreadyExists) {
            throw new IllegalStateException("Студент уже отправил решение для этого задания");
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content) // <-- без getBytes()
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.SUBMITTED)
                .build();

        return submissionRepository.save(submission);
    }

    @Override
    public Submission gradeSubmission(Long submissionId,
                                      Integer score,
                                      String feedback,
                                      SubmissionStatus status) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Решение не найдено: " + submissionId));

        if (score != null) {
            submission.setScore(score);
        }
        submission.setFeedback(feedback);

        if (status != null) {
            submission.setStatus(status);
        } else {

            submission.setStatus(SubmissionStatus.CHECKED);
        }

        return submissionRepository.save(submission);
    }
}
