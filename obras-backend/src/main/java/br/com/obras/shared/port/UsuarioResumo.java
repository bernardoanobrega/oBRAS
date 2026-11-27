package br.com.obras.shared.port;

/** Dados mínimos de um usuário, expostos a outros módulos. */
public record UsuarioResumo(Long id, String nome, boolean estaAtivo) {
}