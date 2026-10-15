package backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata erros de validação dos campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(
            MethodArgumentNotValidException exception) {

        Map<String, String> erros = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    // Trata tentativa de cadastro com e-mail duplicado
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarEmailDuplicado(
            IllegalArgumentException exception) {

        Map<String, String> erro = new HashMap<>();

        erro.put("email", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    // Trata erros HTTP, como 404 e 409
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarResponseStatus(
            ResponseStatusException exception) {

        Map<String, String> erro = new HashMap<>();

        erro.put("message", exception.getReason());

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(erro);
    }
}