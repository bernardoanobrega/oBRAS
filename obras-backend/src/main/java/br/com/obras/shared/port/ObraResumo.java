package br.com.obras.shared.port;

/** Dados mínimos de uma obra, expostos a outros módulos. */
public record ObraResumo(Long id, String nome, boolean estaEncerrada) {
}