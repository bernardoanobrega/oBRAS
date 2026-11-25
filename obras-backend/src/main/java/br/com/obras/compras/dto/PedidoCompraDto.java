package br.com.obras.compras.dto;

import br.com.obras.compras.domain.PrioridadePedido;
import br.com.obras.compras.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoCompraDto(Long id, Long obraId, Long solicitanteId, Long fornecedorId, String fornecedorNome,
                              LocalDate dataSolicitacao, LocalDate dataPrevista, BigDecimal valorTotal,
                              StatusPedido status, PrioridadePedido prioridade, boolean atrasado,
                              List<ItemPedidoDto> itens, List<CotacaoDto> cotacoes,
                              List<HistoricoStatusDto> historico) {}
