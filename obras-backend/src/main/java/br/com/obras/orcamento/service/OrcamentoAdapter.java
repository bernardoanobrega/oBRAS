package br.com.obras.orcamento.service;

import br.com.obras.orcamento.domain.ItemOrcado;
import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.OrigemCusto;
import br.com.obras.orcamento.repository.OrcamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.port.OrcamentoPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Orçamento visto por Compras (RF-33): consulta de item orçado e lançamento do custo de material
 * quando um pedido de compra é efetivado. As regras de custo continuam no domínio.
 */
@Component
public class OrcamentoAdapter implements OrcamentoPort {
    private final OrcamentoRepository orcamentos;

    public OrcamentoAdapter(OrcamentoRepository orcamentos) {
        this.orcamentos = orcamentos;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemOrcadoResumo> buscarItem(Long itemOrcadoId) {
        return orcamentos.findByItensId(itemOrcadoId)
                .map(orcamento -> resumir(orcamento, orcamento.buscarItem(itemOrcadoId)));
    }

    /** Lança o custo com origem MATERIAL e idOrigem = pedido; recalcula a saúde do orçamento. */
    @Override
    @Transactional
    public void registrarCustoMaterial(Long itemOrcadoId, String descricao, BigDecimal valor,
                                       Long pedidoCompraId, Long autorId) {
        Orcamento orcamento = orcamentos.findByItensId(itemOrcadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item orçado", itemOrcadoId));
        orcamento.buscarItem(itemOrcadoId)
                .registrarCusto(descricao, valor, OrigemCusto.MATERIAL, pedidoCompraId, autorId);
    }

    private static ItemOrcadoResumo resumir(Orcamento orcamento, ItemOrcado item) {
        return new ItemOrcadoResumo(item.getId(), orcamento.getObraId(), item.calcularValorOrcado(),
                item.calcularTotalRealizado());
    }
}
