package ua.kpi.blindpig.billing.web.page;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ua.kpi.blindpig.common.BusinessConflictException;
import ua.kpi.blindpig.common.InvalidRequestException;
import ua.kpi.blindpig.common.ResourceNotFoundException;

/** Renders billing HTML errors as a page (REST errors are handled separately in web.api). */
@ControllerAdvice(basePackages = "ua.kpi.blindpig.billing.web.page")
public class BillingPageExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(ResourceNotFoundException e, Model model) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), model);
    }

    @ExceptionHandler({InvalidRequestException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(Exception e, Model model) {
        return error(HttpStatus.BAD_REQUEST, e instanceof InvalidRequestException ? e.getMessage() : "Некоректний параметр запиту", model);
    }

    @ExceptionHandler(BusinessConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflict(BusinessConflictException e, Model model) {
        return error(HttpStatus.CONFLICT, e.getMessage(), model);
    }

    private static String error(HttpStatus status, String message, Model model) {
        model.addAttribute("status", status.value());
        model.addAttribute("message", message);
        return "billing/error";
    }
}
