package examples.spring.project.exception;

import examples.spring.project.Body;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Body> handleInvalidJwtAuthenticationException(final Exception exception,
                                                                        final HttpServletRequest request) {

        Map<String, Object> param = new HashMap<>();
        param.put("uri", request.getRequestURI());
        param.put("error_timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Body.build().fail(exception.getMessage(), param));
    }
}
