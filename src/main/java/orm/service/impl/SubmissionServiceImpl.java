package orm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orm.entity.Assignment;
import orm.entity.Submission;
import orm.entity.User;
import orm.model.SubmissionStatus;
import orm.repository.AssignmentRepository;
import orm.repository.EnrollmentRepository;
import orm.repository.SubmissionRepository;
import orm.repository.UserRepository;
import orm.service.SubmissionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Submission submitAssignment(Long studentId, Long assignmentId, String content) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + studentId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        Long courseId = assignment.getLesson().getModule().getCourse().getId();
        if (!enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId)) {
            throw new IllegalStateException("Student is not enrolled in the course");
        }

        if (submissionRepository.existsByAssignment_IdAndStudent_Id(assignmentId, studentId)) {
            throw new IllegalStateException("Submission already exists for this assignment and student");
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        return submissionRepository.save(submission);
    }

    @Override
    public Submission gradeSubmission(Long submissionId, Integer score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionStatus.CHECKED);

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsForAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_Id(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsForStudent(Long studentId) {
        return submissionRepository.findByStudent_Id(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Submission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
    }
}
