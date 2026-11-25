package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.ItemOrcado;
import br.com.obras.orcamento.domain.SaudeFinanceira;

import java.math.BigDecimal;

public record ItemOrcadoDto(Long id, String codigo, String descricao, String unidade, BigDecimal quantidade,
                            BigDecimal custoUnitarioOrcado, BigDecimal valorOrcado, BigDecimal totalRealizado,
                            BigDecimal desvioFinanceiro, BigDecimal percentualDesvio,
                            BigDecimal percentualExecutado, boolean estaEmEstouro, SaudeFinanceira saude,
                            int quantidadeCustos) {
    public static ItemOrcadoDto de(ItemOrcado item) {
        BigDecimal limiteAlerta = item.getOrcamento().getPercentualLimiteAlerta();
        return new ItemOrcadoDto(item.getId(), item.getCodigo(), item.getDescricao(), item.getUnidade(),
                item.getQuantidade(), item.getCustoUnitarioOrcado(), item.calcularValorOrcado(),
                item.calcularTotalRealizado(), item.calcularDesvioFinanceiro(), item.calcularPercentualDesvio(),
                item.calcularPercentualExecutado(), item.estaEmEstouro(), item.classificarSaude(limiteAlerta),
                item.getCustos().size());
    }
}
