package br.com.obras.estoque.dto;

/** emAlerta = true => o front oferece "gerar solicitação de reposição". */
public record SaidaResultadoDto(MovimentacaoDto movimentacao, SaldoDto saldo, boolean emAlerta) {}
