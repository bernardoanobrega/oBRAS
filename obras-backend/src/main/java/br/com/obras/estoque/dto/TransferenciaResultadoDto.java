package br.com.obras.estoque.dto;

public record TransferenciaResultadoDto(MovimentacaoDto movimentacao, SaldoDto saldoOrigem,
                                        SaldoDto saldoDestino, boolean emAlertaOrigem) {}
