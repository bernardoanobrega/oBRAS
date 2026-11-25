package br.com.obras.diario.repository;

/** Total de ocorrências ativas de um diário; resultado da contagem agrupada. */
public record ContagemOcorrencias(Long diarioId, Long total) {}
