package br.com.obras.orcamento.service;

import br.com.obras.orcamento.domain.CustoRealizado;
import br.com.obras.orcamento.domain.ItemOrcado;
import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.OrigemCusto;
import br.com.obras.orcamento.domain.StatusOrcamento;
import br.com.obras.orcamento.repository.OrcamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.OrcamentoPort.ItemOrcadoResumo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoAdapterTest {
    private static final Long OBRA_ID = 3L;
    private static final Long ITEM_ID = 7L;
    private static final Long PEDIDO_ID = 40L;
    private static final Long AUTOR_ID = 9L;
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    @Mock
    private OrcamentoRepository orcamentos;

    private OrcamentoAdapter adapter;
    private Orcamento orcamento;
    private ItemOrcado item;

    @BeforeEach
    void configurar() {
        adapter = new OrcamentoAdapter(orcamentos);
        orcamento = Orcamento.elaborar(OBRA_ID, LIMITE_ALERTA);
        item = orcamento.adicionarItem("01", "Fundação", "m3", BigDecimal.TEN, new BigDecimal("100"));
        ReflectionTestUtils.setField(item, "id", ITEM_ID);
    }

    @Test
    void dadoItemExistenteQuandoBuscarEntaoRetornaResumoComObraEValores() {
        when(orcamentos.findByItensId(ITEM_ID)).thenReturn(Optional.of(orcamento));

        ItemOrcadoResumo resumo = adapter.buscarItem(ITEM_ID).orElseThrow();

        assertEquals(ITEM_ID, resumo.id());
        assertEquals(OBRA_ID, resumo.obraId());
        assertEquals(new BigDecimal("1000.00"), resumo.valorOrcado());
        assertEquals(new BigDecimal("0.00"), resumo.totalRealizado());
    }

    @Test
    void dadoItemInexistenteQuandoBuscarEntaoRetornaVazio() {
        when(orcamentos.findByItensId(ITEM_ID)).thenReturn(Optional.empty());

        assertTrue(adapter.buscarItem(ITEM_ID).isEmpty());
    }

    @Test
    void dadoPedidoEfetivadoQuandoRegistrarCustoMaterialEntaoLancaCustoVinculadoAoPedido() {
        when(orcamentos.findByItensId(ITEM_ID)).thenReturn(Optional.of(orcamento));

        adapter.registrarCustoMaterial(ITEM_ID, "Pedido de compra #40", new BigDecimal("950"), PEDIDO_ID,
                AUTOR_ID);

        CustoRealizado custo = item.getCustos().get(0);
        assertEquals(OrigemCusto.MATERIAL, custo.getOrigem());
        assertEquals(PEDIDO_ID, custo.getIdOrigem());
        assertTrue(custo.estaVinculadoAOutroModulo());
        assertEquals(StatusOrcamento.ATENCAO, orcamento.getStatus());
    }

    @Test
    void dadoItemInexistenteQuandoRegistrarCustoMaterialEntaoLancaNaoEncontrado() {
        when(orcamentos.findByItensId(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> adapter.registrarCustoMaterial(ITEM_ID,
                "Pedido de compra #40", BigDecimal.TEN, PEDIDO_ID, AUTOR_ID));
    }

    @Test
    void dadoOrcamentoCanceladoQuandoRegistrarCustoMaterialEntaoLancaRegraNegocio() {
        orcamento.cancelar();
        when(orcamentos.findByItensId(ITEM_ID)).thenReturn(Optional.of(orcamento));

        assertThrows(RegraNegocioException.class, () -> adapter.registrarCustoMaterial(ITEM_ID,
                "Pedido de compra #40", BigDecimal.TEN, PEDIDO_ID, AUTOR_ID));
    }
}
