package br.com.obras.shared.exception;

/** Violação de regra de negócio / transição de estado inválida. HTTP 422. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) { super(mensagem); }
}
