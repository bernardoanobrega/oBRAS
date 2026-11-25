package br.com.obras.diario.dto;

/** Pessoa da obra citada no diário: integrante da equipe presente ou autor de ocorrência. */
public record MembroDiarioDto(Long alocacaoId, Long usuarioId, String nome) {}
