package br.com.obras.shared.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a): " + id);
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}