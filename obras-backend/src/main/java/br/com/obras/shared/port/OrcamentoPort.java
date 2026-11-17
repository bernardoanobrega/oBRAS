package br.com.obras.shared.port;

import java.math.BigDecimal;
import java.util.Optional;

/** Orçamento visto por compras. Implementada pelo módulo orcamento (ainda não existe). */
public interface OrcamentoPort {
    Optional<ItemOrcadoResumo> buscarItem(Long itemOrcadoId);

    /** Lança CustoRealizado com origem MATERIAL e idOrigem = pedido. */
    void registrarCustoMaterial(Long itemOrcadoId, String descricao, BigDecimal valor,
                                Long pedidoCompraId, Long autorId);

    record ItemOrcadoResumo(Long id, Long obraId, BigDecimal valorOrcado, BigDecimal totalRealizado) {}
}
