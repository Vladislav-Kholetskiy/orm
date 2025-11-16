package orm.service;

import orm.entity.Assignment;
import orm.entity.Submission;
import orm.entity.User;
import orm.model.Role;
import orm.model.SubmissionStatus;
import orm.repository.AssignmentRepository;
import orm.repository.SubmissionRepository;
import orm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import orm.service.impl.AssignmentServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;


    @Test
    void submitAssignment_createsSubmissionIfNotExists() {
        Long assignmentId = 1L;
        Long studentId = 2L;
        String content = "My solution";

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .title("HW1")
                .maxScore(100)
                .build();

        User student = User.builder()
                .id(studentId)
                .name("Student")
                .email("student@example.com")
                .password("pwd")
                .role(Role.STUDENT)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(submissionRepository.existsByAssignment_IdAndStudent_Id(assignmentId, studentId)).thenReturn(false);

        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        Submission submission = assignmentService.submitAssignment(assignmentId, studentId, content);

        assertThat(submission.getId()).isEqualTo(100L);
        assertThat(submission.getAssignment()).isEqualTo(assignment);
        assertThat(submission.getStudent()).isEqualTo(student);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submission.getSubmittedAt()).isNotNull();
    }

    @Test
    void submitAssignment_throwsIfAlreadySubmitted() {
        Long assignmentId = 1L;
        Long studentId = 2L;

        Assignment assignment = Assignment.builder()
                .id(assignmentId)
                .title("HW1")
                .maxScore(100)
                .build();

        User student = User.builder()
                .id(studentId)
                .name("Student")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        when(submissionRepository.existsByAssignment_IdAndStudent_Id(assignmentId, studentId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                assignmentService.submitAssignment(assignmentId, studentId, "content")
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже отправил решение");

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void gradeSubmission_setsScoreFeedbackAndStatusChecked() {
        Long submissionId = 10L;
        int score = 90;
        String feedback = "Good job";

        Submission submission = Submission.builder()
                .id(submissionId)
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Submission graded = assignmentService.gradeSubmission(submissionId, score, feedback, SubmissionStatus.CHECKED);

        assertThat(graded.getScore()).isEqualTo(score);
        assertThat(graded.getFeedback()).isEqualTo(feedback);
        assertThat(graded.getStatus()).isEqualTo(SubmissionStatus.CHECKED);
        verify(submissionRepository, times(1)).save(submission);
    }
}
