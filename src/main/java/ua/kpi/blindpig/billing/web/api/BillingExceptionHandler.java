package ua.kpi.blindpig.billing.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import ua.kpi.blindpig.billing.web.api.dto.BillingApiError;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/** JSON errors for billing REST controllers only (HTML pages use BillingPageExceptionHandler). */
@RestControllerAdvice(basePackages = "ua.kpi.blindpig.billing.web.api")
public class BillingExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BillingApiError> notFound(ResourceNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<BillingApiError> conflict(BusinessConflictException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<BillingApiError> invalid(InvalidRequestException e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BillingApiError> validation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(BillingExceptionHandler::describe)
                .sorted()
                .toList();
        return respond(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BillingApiError> unreadable(HttpMessageNotReadableException e) {
        // Jackson 3 rejects missing/null primitives (e.g. "active") and unknown enum names; name the field when known.
        if (e.getMostSpecificCause() instanceof JacksonException je && !je.getPath().isEmpty()) {
            String field = je.getPath().stream()
                    .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."));
            return respond(HttpStatus.BAD_REQUEST, "Invalid or missing value for field '" + field + "'", List.of());
        }
        return respond(HttpStatus.BAD_REQUEST, "Malformed JSON request body", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BillingApiError> typeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + e.getName() + "'", List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BillingApiError> missingParam(MissingServletRequestParameterException e) {
        return respond(HttpStatus.BAD_REQUEST, "Missing required parameter '" + e.getParameterName() + "'", List.of());
    }

    private static String describe(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<BillingApiError> respond(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status).body(BillingApiError.of(status.value(), status.getReasonPhrase(), message, details));
    }
}
