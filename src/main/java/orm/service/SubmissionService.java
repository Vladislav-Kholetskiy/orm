package orm.service;

import orm.entity.Submission;

import java.util.List;

public interface SubmissionService {

    Submission submitAssignment(Long studentId, Long assignmentId, String content);

    Submission gradeSubmission(Long submissionId, Integer score, String feedback);

    List<Submission> getSubmissionsForAssignment(Long assignmentId);

    List<Submission> getSubmissionsForStudent(Long studentId);

    Submission getSubmission(Long submissionId);
}
