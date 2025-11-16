package orm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import orm.entity.AnswerOption;
import orm.entity.Question;
import orm.entity.Quiz;
import orm.entity.QuizSubmission;
import orm.model.QuestionType;
import orm.service.QuizService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/modules/{moduleId}")
    public Quiz createQuizForModule(@PathVariable Long moduleId,
                                    @Valid @RequestBody CreateQuizRequest request) {
        Quiz quiz = Quiz.builder()
                .title(request.title())
                .timeLimitMinutes(request.timeLimitMinutes())
                .build();

        if (request.questions() != null) {
            for (CreateQuestionRequest qReq : request.questions()) {
                Question question = Question.builder()
                        .text(qReq.text())
                        .type(qReq.type())
                        .build();

                if (qReq.options() != null) {
                    for (CreateAnswerOptionRequest oReq : qReq.options()) {
                        AnswerOption option = AnswerOption.builder()
                                .text(oReq.text())
                                .isCorrect(oReq.correct())
                                .build();
                        question.getOptions().add(option);
                        option.setQuestion(question);
                    }
                }

                quiz.getQuestions().add(question);
                question.setQuiz(quiz);
            }
        }

        return quizService.createQuizForModule(moduleId, quiz);
    }

    @GetMapping("/{quizId}")
    public Quiz getQuiz(@PathVariable Long quizId) {
        return quizService.getQuiz(quizId);
    }

    @PostMapping("/{quizId}/submit")
    public QuizSubmission submitQuiz(@PathVariable Long quizId,
                                     @Valid @RequestBody TakeQuizRequest request) {
        return quizService.takeQuiz(
                request.studentId(),
                quizId,
                request.answers()
        );
    }

    @GetMapping("/{quizId}/submissions")
    public List<QuizSubmission> getSubmissionsForQuiz(@PathVariable Long quizId) {
        return quizService.getSubmissionsForQuiz(quizId);
    }

    @GetMapping("/students/{studentId}/submissions")
    public List<QuizSubmission> getSubmissionsForStudent(@PathVariable Long studentId) {
        return quizService.getSubmissionsForStudent(studentId);
    }

    // --- DTO ---

    public record CreateQuizRequest(
            @NotBlank(message = "Title is required")
            String title,

            Integer timeLimitMinutes,

            @NotEmpty(message = "Questions are required")
            @Valid
            List<CreateQuestionRequest> questions
    ) {
    }

    public record CreateQuestionRequest(
            @NotBlank(message = "Question text is required")
            String text,

            @NotNull(message = "Question type is required")
            QuestionType type,

            @NotEmpty(message = "Answer options are required")
            @Valid
            List<CreateAnswerOptionRequest> options
    ) {
    }

    public record CreateAnswerOptionRequest(
            @NotBlank(message = "Option text is required")
            String text,

            boolean correct
    ) {
    }

    /**
     * answers:
     *  key: questionId
     *  value: список выбранных optionId
     */
    public record TakeQuizRequest(
            @NotNull(message = "Student ID is required")
            Long studentId,

            @NotNull(message = "Answers are required")
            Map<Long, List<Long>> answers
    ) {
    }
}
