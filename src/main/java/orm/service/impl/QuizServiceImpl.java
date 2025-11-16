package orm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import orm.entity.*;
import orm.entity.Module;
import orm.repository.AnswerOptionRepository;
import orm.repository.ModuleRepository;
import orm.repository.QuizRepository;
import orm.repository.QuizSubmissionRepository;
import orm.repository.UserRepository;
import orm.service.QuizService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    @Override
    public Quiz createQuizForModule(Long moduleId, Quiz quiz) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));

        quiz.setModule(module);

        // Связать Question/AnswerOption с Quiz/Question (если сущности уже заполнены)
        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(q -> {
                q.setQuiz(quiz);
                if (q.getOptions() != null) {
                    q.getOptions().forEach(o -> o.setQuestion(q));
                }
            });
        }

        return quizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Quiz getQuiz(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
    }

    @Override
    public QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, List<Long>> answersByQuestionId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + studentId));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));

        int totalQuestions = quiz.getQuestions().size();
        int correctQuestions = 0;

        for (Question question : quiz.getQuestions()) {
            List<Long> selectedOptionIds = answersByQuestionId.getOrDefault(question.getId(), List.of());

            Set<Long> correctOptionIds = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());

            Set<Long> selectedSet = new HashSet<>(selectedOptionIds);

            if (selectedSet.equals(correctOptionIds)) {
                correctQuestions++;
            }
        }

        int score = totalQuestions == 0 ? 0 : (int) Math.round((correctQuestions * 100.0) / totalQuestions);
        boolean passed = score >= 50; // условно: 50%+ правильных — "сдал"

        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .score(score)
                .passed(passed)
                .build();

        return quizSubmissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSubmission> getSubmissionsForQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuiz_Id(quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSubmission> getSubmissionsForStudent(Long studentId) {
        return quizSubmissionRepository.findByStudent_Id(studentId);
    }
}
