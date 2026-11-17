package br.com.obras.compras.service;

import br.com.obras.shared.port.OrcamentoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * PROVISÓRIO: o módulo orcamento ainda não existe. Enquanto isso, nenhum item de orçamento é encontrado
 * (vincular itemOrcadoId retorna 404) e o lançamento de custo vira log.
 * REMOVER esta classe quando o orcamento implementar OrcamentoPort.
 */
@Component
public class OrcamentoPortProvisorio implements OrcamentoPort {
    private static final Logger log = LoggerFactory.getLogger(OrcamentoPortProvisorio.class);

    @Override
    public Optional<ItemOrcadoResumo> buscarItem(Long itemOrcadoId) {
        return Optional.empty();
    }

    @Override
    public void registrarCustoMaterial(Long itemOrcadoId, String descricao, BigDecimal valor,
                                       Long pedidoCompraId, Long autorId) {
        log.warn("Orçamento indisponível: custo de {} do pedido {} (item orçado {}) NÃO foi lançado",
                valor, pedidoCompraId, itemOrcadoId);
    }
}
