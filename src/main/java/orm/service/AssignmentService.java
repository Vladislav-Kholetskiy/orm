package orm.service;

import orm.entity.Assignment;
import orm.entity.Submission;
import orm.model.SubmissionStatus;

import java.util.List;

public interface AssignmentService {

    Assignment createAssignment(Long lessonId, Assignment assignment);

    Assignment updateAssignment(Long assignmentId, Assignment updatedData);

    void deleteAssignment(Long assignmentId);

    Assignment getAssignment(Long assignmentId);

    List<Assignment> getAssignmentsByLesson(Long lessonId);


    Submission submitAssignment(Long assignmentId, Long studentId, String content);


    Submission gradeSubmission(Long submissionId,
                               Integer score,
                               String feedback,
                               SubmissionStatus status);
}
