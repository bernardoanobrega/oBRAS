package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.CustoRealizado;
import br.com.obras.orcamento.domain.OrigemCusto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** vinculadoAOutroModulo: custo lançado por outro módulo, que não pode ser editado nem removido aqui. */
public record CustoRealizadoDto(Long id, Long itemId, LocalDate dataLancamento, String descricao, BigDecimal valor,
                                OrigemCusto origem, Long idOrigem, Long usuarioId, boolean vinculadoAOutroModulo) {
    public static CustoRealizadoDto de(CustoRealizado custo) {
        return new CustoRealizadoDto(custo.getId(), custo.getItem().getId(), custo.getDataLancamento(),
                custo.getDescricao(), custo.getValor(), custo.getOrigem(), custo.getIdOrigem(), custo.getUsuarioId(),
                custo.estaVinculadoAOutroModulo());
    }
}
