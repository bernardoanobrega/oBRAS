package br.com.obras.orcamento.domain;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemOrcadoTest {
    private static final Long USUARIO_ID = 7L;
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    private ItemOrcado novoItem(String quantidade, String custoUnitario) {
        Orcamento orcamento = Orcamento.elaborar(1L, LIMITE_ALERTA);
        return orcamento.adicionarItem("02.01", "Alvenaria", "m2", new BigDecimal(quantidade),
                new BigDecimal(custoUnitario));
    }

    @Test
    void calculaValorOrcadoArredondandoMeioParaCima() {
        ItemOrcado item = novoItem("2.5", "10.33");

        assertEquals(new BigDecimal("25.83"), item.calcularValorOrcado());
    }

    @Test
    void calculaDesvioEPercentuais() {
        ItemOrcado item = novoItem("10", "100");

        item.registrarCusto("Blocos", new BigDecimal("1040"), OrigemCusto.MATERIAL, 3L, USUARIO_ID);

        assertEquals(new BigDecimal("40.00"), item.calcularDesvioFinanceiro());
        assertEquals(new BigDecimal("4.00"), item.calcularPercentualDesvio());
        assertEquals(new BigDecimal("104.00"), item.calcularPercentualExecutado());
        assertTrue(item.estaEmEstouro());
        assertEquals(SaudeFinanceira.CRITICA, item.classificarSaude(LIMITE_ALERTA));
    }

    @Test
    void itemDentroDoLimiteEstaSaudavel() {
        ItemOrcado item = novoItem("10", "100");

        item.registrarCusto("Blocos", new BigDecimal("300"), OrigemCusto.MATERIAL, null, USUARIO_ID);

        assertFalse(item.estaEmEstouro());
        assertEquals(SaudeFinanceira.SAUDAVEL, item.classificarSaude(LIMITE_ALERTA));
    }

    @Test
    void itemQueAtingeLimiteFicaEmAtencao() {
        ItemOrcado item = novoItem("10", "100");

        item.registrarCusto("Blocos", new BigDecimal("950"), OrigemCusto.MATERIAL, null, USUARIO_ID);

        assertEquals(SaudeFinanceira.ATENCAO, item.classificarSaude(LIMITE_ALERTA));
    }

    @Test
    void lancaExcecaoQuandoCustoTemValorNaoPositivo() {
        ItemOrcado item = novoItem("10", "100");

        assertThrows(RegraNegocioException.class, () -> item.registrarCusto("Blocos", BigDecimal.ZERO,
                OrigemCusto.MATERIAL, null, USUARIO_ID));
        assertFalse(item.possuiCustos());
    }

    @Test
    void lancaExcecaoQuandoOrigemOutroTemReferencia() {
        ItemOrcado item = novoItem("10", "100");

        assertThrows(RegraNegocioException.class, () -> item.registrarCusto("Taxa", BigDecimal.TEN,
                OrigemCusto.OUTRO, 5L, USUARIO_ID));
    }

    private CustoRealizado lancarCustoComId(ItemOrcado item, String valor, Long idOrigem, Long idCusto) {
        CustoRealizado custo = item.registrarCusto("Blocos", new BigDecimal(valor), OrigemCusto.MATERIAL, idOrigem,
                USUARIO_ID);
        ReflectionTestUtils.setField(custo, "id", idCusto);
        return custo;
    }

    @Test
    void editarCustoCorrigeValorEReclassificaOrcamento() {
        ItemOrcado item = novoItem("10", "100");
        lancarCustoComId(item, "1100", null, 1L);

        item.editarCusto(1L, "Blocos (corrigido)", new BigDecimal("400"), OrigemCusto.MAO_DE_OBRA);

        CustoRealizado custo = item.buscarCusto(1L);
        assertEquals(new BigDecimal("400.00"), custo.getValor());
        assertEquals(OrigemCusto.MAO_DE_OBRA, custo.getOrigem());
        assertEquals(StatusOrcamento.SAUDAVEL, item.getOrcamento().getStatus());
    }

    @Test
    void lancaExcecaoAoEditarCustoComValorInvalidoSemAlterarNada() {
        ItemOrcado item = novoItem("10", "100");
        lancarCustoComId(item, "300", null, 1L);

        assertThrows(RegraNegocioException.class,
                () -> item.editarCusto(1L, "Blocos", BigDecimal.ZERO, OrigemCusto.MATERIAL));
        assertEquals(new BigDecimal("300.00"), item.buscarCusto(1L).getValor());
    }

    @Test
    void removerCustoRecalculaRealizado() {
        ItemOrcado item = novoItem("10", "100");
        lancarCustoComId(item, "1100", null, 1L);
        lancarCustoComId(item, "200", null, 2L);

        item.removerCusto(1L);

        assertEquals(new BigDecimal("200.00"), item.calcularTotalRealizado());
        assertEquals(StatusOrcamento.SAUDAVEL, item.getOrcamento().getStatus());
    }

    @Test
    void lancaExcecaoAoAlterarCustoVinculadoAOutroModulo() {
        ItemOrcado item = novoItem("10", "100");
        lancarCustoComId(item, "300", 55L, 1L);

        assertThrows(RegraNegocioException.class,
                () -> item.editarCusto(1L, "Blocos", BigDecimal.TEN, OrigemCusto.MATERIAL));
        assertThrows(RegraNegocioException.class, () -> item.removerCusto(1L));
    }

    @Test
    void lancaExcecaoAoAlterarCustoDeOrcamentoCancelado() {
        ItemOrcado item = novoItem("10", "100");
        lancarCustoComId(item, "300", null, 1L);
        item.getOrcamento().cancelar();

        assertThrows(RegraNegocioException.class, () -> item.removerCusto(1L));
    }

    @Test
    void lancaExcecaoQuandoCustoNaoExiste() {
        assertThrows(RecursoNaoEncontradoException.class, () -> novoItem("10", "100").removerCusto(99L));
    }

    @Test
    void lancaExcecaoQuandoQuantidadeNaoPositiva() {
        assertThrows(RegraNegocioException.class, () -> novoItem("0", "100"));
    }

    @Test
    void lancaExcecaoQuandoCustoUnitarioNegativo() {
        assertThrows(RegraNegocioException.class, () -> novoItem("1", "-0.01"));
    }
}
