package br.com.obras.shared.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(ConcurrencyFailureException.class)
    ResponseEntity<ApiError> concorrencia(ConcurrencyFailureException e) {
        log.warn("Falha de concorrência", e);
        return resp(HttpStatus.CONFLICT, "O registro foi alterado por outra operação. Tente novamente.");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> requisicaoInvalida(Exception e) {
        return resp(HttpStatus.BAD_REQUEST, "Requisição inválida. Confira os campos e o arquivo enviados.");
    }

    /**
     * JSON que não pôde ser convertido no DTO (ex.: valor fora do enum, data mal
     * formatada, JSON quebrado). Acontece antes da Bean Validation, então não cai
     * no handler acima. É erro do cliente: 400, apontando o campo quando possível.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> corpoIlegivel(HttpMessageNotReadableException e) {
        List<ApiError.CampoErro> campos = List.of();
        if (e.getCause() instanceof JsonMappingException jme && !jme.getPath().isEmpty()) {
            String campo = jme.getPath().stream()
                    .map(r -> r.getFieldName() != null ? r.getFieldName() : "[" + r.getIndex() + "]")
                    .collect(Collectors.joining("."));
            campos = List.of(new ApiError.CampoErro(campo, "valor inválido"));
        }
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
