package orm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import orm.entity.Submission;
import orm.service.SubmissionService;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/assignments/{assignmentId}")
    public Submission submitAssignment(@PathVariable Long assignmentId,
                                       @Valid @RequestBody SubmitAssignmentRequest request) {
        return submissionService.submitAssignment(
                request.studentId(),
                assignmentId,
                request.content()
        );
    }

    @PostMapping("/{submissionId}/grade")
    public Submission gradeSubmission(@PathVariable Long submissionId,
                                      @Valid @RequestBody GradeSubmissionRequest request) {
        return submissionService.gradeSubmission(
                submissionId,
                request.score(),
                request.feedback()
        );
    }

    @GetMapping("/{submissionId}")
    public Submission getSubmission(@PathVariable Long submissionId) {
        return submissionService.getSubmission(submissionId);
    }

    @GetMapping("/students/{studentId}")
    public List<Submission> getSubmissionsForStudent(@PathVariable Long studentId) {
        return submissionService.getSubmissionsForStudent(studentId);
    }

    @GetMapping("/assignments/{assignmentId}")
    public List<Submission> getSubmissionsForAssignment(@PathVariable Long assignmentId) {
        return submissionService.getSubmissionsForAssignment(assignmentId);
    }

    public record SubmitAssignmentRequest(
            @NotNull(message = "Student ID is required")
            Long studentId,

            @NotBlank(message = "Content is required")
            String content
    ) {
    }

    public record GradeSubmissionRequest(
            @NotNull(message = "Score is required")
            @PositiveOrZero(message = "Score must be >= 0")
            Integer score,

            @NotBlank(message = "Feedback is required")
            String feedback
    ) {
    }
}
