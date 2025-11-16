package orm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import orm.entity.Assignment;
import orm.service.AssignmentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/lessons/{lessonId}")
    public Assignment createAssignment(@PathVariable Long lessonId,
                                       @Valid @RequestBody CreateAssignmentRequest request) {
        Assignment assignment = Assignment.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .maxScore(request.maxScore())
                .build();
        return assignmentService.createAssignment(lessonId, assignment);
    }

    @GetMapping("/{id}")
    public Assignment getAssignment(@PathVariable Long id) {
        return assignmentService.getAssignment(id);
    }

    @GetMapping("/lessons/{lessonId}")
    public List<Assignment> getAssignmentsForLesson(@PathVariable Long lessonId) {
        return assignmentService.getAssignmentsByLesson(lessonId);
    }

    public record CreateAssignmentRequest(
            @NotBlank(message = "Title is required")
            String title,

            @NotBlank(message = "Description is required")
            String description,

            @NotNull(message = "Due date is required")
            LocalDateTime dueDate,

            @NotNull(message = "Max score is required")
            @Positive(message = "Max score must be positive")
            Integer maxScore
    ) {
    }
}
