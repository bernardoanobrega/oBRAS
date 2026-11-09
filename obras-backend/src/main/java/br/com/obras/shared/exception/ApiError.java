package br.com.obras.shared.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(int status, String mensagem, List<CampoErro> campos, Instant timestamp) {
    public record CampoErro(String campo, String mensagem) {}

    public static ApiError of(int status, String mensagem) {
        return new ApiError(status, mensagem, List.of(), Instant.now());
    }
}
