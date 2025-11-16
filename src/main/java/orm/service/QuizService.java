package orm.service;

import orm.entity.Quiz;
import orm.entity.QuizSubmission;

import java.util.List;
import java.util.Map;

public interface QuizService {

    Quiz createQuizForModule(Long moduleId, Quiz quiz);

    Quiz getQuiz(Long quizId);

    /**
     * answersByQuestionId:
     *   key: questionId
     *   value: список id выбранных вариантов ответов (AnswerOption.id)
     */
    QuizSubmission takeQuiz(Long studentId, Long quizId, Map<Long, List<Long>> answersByQuestionId);

    List<QuizSubmission> getSubmissionsForQuiz(Long quizId);

    List<QuizSubmission> getSubmissionsForStudent(Long studentId);
}

