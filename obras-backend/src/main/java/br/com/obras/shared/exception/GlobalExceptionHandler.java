package br.com.obras.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;

/** Formato único de erro para toda a API (RNF-09). */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ApiError> regra(RegraNegocioException e) {
        return resp(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException e) {
        return resp(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<ApiError> credenciais(CredenciaisInvalidasException e) {
        return resp(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> negado(AccessDeniedException e) {
        return resp(HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> upload(MaxUploadSizeExceededException e) {
        return resp(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo excede o limite de 10 MB");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade", e);
        return resp(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou em uso)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validacao(MethodArgumentNotValidException e) {
        List<ApiError.CampoErro> campos = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.CampoErro(f.getField(), f.getDefaultMessage())).toList();
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Dados inválidos", campos, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> generico(Exception e) {
        log.error("Erro não tratado", e);
        return resp(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.");
    }

    private ResponseEntity<ApiError> resp(HttpStatus s, String msg) {
        return ResponseEntity.status(s).body(ApiError.of(s.value(), msg));
    }
}
