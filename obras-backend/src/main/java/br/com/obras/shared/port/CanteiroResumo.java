package br.com.obras.shared.port;

/** Dados mínimos de um canteiro de obras, expostos a outros módulos. */
public record CanteiroResumo(Long id, Long obraId, String nome) {
}