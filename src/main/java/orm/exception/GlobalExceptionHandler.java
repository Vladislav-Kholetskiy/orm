package orm.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Валидация @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {

        List<ApiValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation error")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .validationErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    private ApiValidationError mapFieldError(FieldError fe) {
        String field = fe.getField();
        String message = fe.getDefaultMessage();
        return new ApiValidationError(field, message);
    }

    // Валидация @Validated на параметрах/сервисах (@Min, @NotNull и т.д.)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {

        List<ApiValidationError> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::mapConstraintViolation)
                .toList();

        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation error")
                .message("Constraint violation")
                .path(request.getRequestURI())
                .validationErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    private ApiValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        // propertyPath типа "createCourse.request.title"
        String path = violation.getPropertyPath() != null
                ? violation.getPropertyPath().toString()
                : null;
        String message = violation.getMessage();
        return new ApiValidationError(path, message);
    }

    // Бизнес-ошибки: неверные аргументы, нарушенные предикаты
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        return buildSimpleError(HttpStatus.BAD_REQUEST, "Invalid argument", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex,
                                                       HttpServletRequest request) {
        return buildSimpleError(HttpStatus.CONFLICT, "Invalid state", ex.getMessage(), request);
    }

    // Ошибки БД: нарушение уникальности, FK и т.п.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        return buildSimpleError(HttpStatus.CONFLICT, "Data integrity violation", ex.getMostSpecificCause().getMessage(), request);
    }

    // Фолбек: любая другая необработанная ошибка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex,
                                              HttpServletRequest request) {
        return buildSimpleError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage(), request);
    }

    private ResponseEntity<ApiError> buildSimpleError(HttpStatus status,
                                                      String error,
                                                      String message,
                                                      HttpServletRequest request) {
        ApiError body = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
