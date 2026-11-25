package br.com.obras.orcamento.domain;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Cobre o diagrama orcamento-states: ao menos uma transição válida e uma inválida por estado. */
class OrcamentoTest {
    private static final Long OBRA_ID = 1L;
    private static final Long USUARIO_ID = 7L;
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    private Orcamento novoOrcamento() {
        return Orcamento.elaborar(OBRA_ID, LIMITE_ALERTA);
    }

    /** Item com valor orçado de R$ 1.000,00 (10 x 100). */
    private ItemOrcado adicionarItemDeMil(Orcamento orcamento) {
        ItemOrcado item = orcamento.adicionarItem("01.01", "Fundação", "m3", new BigDecimal("10"),
                new BigDecimal("100"));
        ReflectionTestUtils.setField(item, "id", 1L);
        return item;
    }

    private void lancarCusto(ItemOrcado item, String valor) {
        item.registrarCusto("Concreto", new BigDecimal(valor), OrigemCusto.MATERIAL, null, USUARIO_ID);
    }

    @Nested
    class Elaboracao {
        @Test
        void iniciaEmElaboracao() {
            assertEquals(StatusOrcamento.ELABORACAO, novoOrcamento().getStatus());
        }

        @Test
        void primeiroItemIniciaAcompanhamentoSaudavel() {
            Orcamento orcamento = novoOrcamento();

            adicionarItemDeMil(orcamento);

            assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());
        }

        @Test
        void decisaoSemItensNaoSaiDeElaboracao() {
            Orcamento orcamento = novoOrcamento();

            orcamento.registrarDecisao("Reserva", "Contingência", new BigDecimal("500"), USUARIO_ID);

            assertEquals(StatusOrcamento.ELABORACAO, orcamento.getStatus());
        }

        @Test
        void lancaExcecaoQuandoLimiteDeAlertaForaDoIntervalo() {
            assertThrows(RegraNegocioException.class, () -> Orcamento.elaborar(OBRA_ID, new BigDecimal("101")));
            assertThrows(RegraNegocioException.class, () -> Orcamento.elaborar(OBRA_ID, BigDecimal.ZERO));
        }
    }

    @Nested
    class Saudavel {
        @Test
        void custoAbaixoDoLimiteMantemSaudavel() {
            Orcamento orcamento = novoOrcamento();
            ItemOrcado item = adicionarItemDeMil(orcamento);

            lancarCusto(item, "500");

            assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());
        }

        @Test
        void custoQueAtingeLimiteVaiParaAtencao() {
            Orcamento orcamento = novoOrcamento();
            ItemOrcado item = adicionarItemDeMil(orcamento);

            lancarCusto(item, "900");

            assertEquals(StatusOrcamento.ATENCAO, orcamento.getStatus());
        }

        @Test
        void lancaExcecaoQuandoCodigoDeItemRepetido() {
            Orcamento orcamento = novoOrcamento();
            adicionarItemDeMil(orcamento);

            assertThrows(RegraNegocioException.class, () -> orcamento.adicionarItem("01.01", "Outro", "un",
                    BigDecimal.ONE, BigDecimal.ONE));
        }
    }

    @Nested
    class Atencao {
        @Test
        void decisaoQueAmpliaOrcadoVoltaParaSaudavel() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "900");

            orcamento.registrarDecisao("Aditivo", "Reajuste do aço", new BigDecimal("200"), USUARIO_ID);

            assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());
        }

        @Test
        void custoAcimaDoOrcadoVaiParaCritica() {
            Orcamento orcamento = novoOrcamento();
            ItemOrcado item = adicionarItemDeMil(orcamento);
            lancarCusto(item, "900");

            lancarCusto(item, "200");

            assertEquals(StatusOrcamento.CRITICA, orcamento.getStatus());
        }
    }

    @Nested
    class Critica {
        @Test
        void decisaoDeContencaoVoltaParaAtencao() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "1100");

            orcamento.registrarDecisao("Remanejamento", "Verba de paisagismo", new BigDecimal("100"), USUARIO_ID);

            assertEquals(StatusOrcamento.ATENCAO, orcamento.getStatus());
        }

        @Test
        void lancaExcecaoQuandoDecisaoDeixaOrcadoNegativo() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "1100");

            assertThrows(RegraNegocioException.class, () -> orcamento.registrarDecisao("Corte", "Corte total",
                    new BigDecimal("-1001"), USUARIO_ID));
        }
    }

    @Nested
    class Cancelado {
        @Test
        void qualquerEstadoAtivoPodeSerCancelado() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "1100");

            orcamento.cancelar();

            assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
        }

        @Test
        void lancaExcecaoAoAlterarOrcamentoCancelado() {
            Orcamento orcamento = novoOrcamento();
            ItemOrcado item = adicionarItemDeMil(orcamento);
            orcamento.cancelar();

            assertThrows(RegraNegocioException.class, () -> lancarCusto(item, "10"));
            assertThrows(RegraNegocioException.class, () -> adicionarItemDeMil(orcamento));
            assertThrows(RegraNegocioException.class, () -> orcamento.registrarDecisao("D", "J",
                    BigDecimal.ONE, USUARIO_ID));
            assertThrows(RegraNegocioException.class, orcamento::cancelar);
        }
    }

    @Nested
    class Itens {
        @Test
        void removeItemSemCustos() {
            Orcamento orcamento = novoOrcamento();
            adicionarItemDeMil(orcamento);

            orcamento.removerItem(1L);

            assertEquals(0, orcamento.getItens().size());
        }

        @Test
        void lancaExcecaoAoRemoverItemComCustos() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "10");

            assertThrows(RegraNegocioException.class, () -> orcamento.removerItem(1L));
        }

        @Test
        void lancaExcecaoQuandoItemNaoExiste() {
            assertThrows(RecursoNaoEncontradoException.class, () -> novoOrcamento().buscarItem(99L));
        }

        @Test
        void edicaoDeValoresReclassificaSaude() {
            Orcamento orcamento = novoOrcamento();
            ItemOrcado item = adicionarItemDeMil(orcamento);
            lancarCusto(item, "900");

            item.atualizarValores(new BigDecimal("20"), new BigDecimal("100"));

            assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());
        }
    }

    @Nested
    class EdicaoDeDecisao {
        private DecisaoFinanceira registrarDecisaoComId(Orcamento orcamento, String valor) {
            DecisaoFinanceira decisao = orcamento.registrarDecisao("Aditivo", "Reajuste", new BigDecimal(valor),
                    USUARIO_ID);
            ReflectionTestUtils.setField(decisao, "id", 1L);
            return decisao;
        }

        @Test
        void novoImpactoSubstituiOAnteriorEReclassifica() {
            Orcamento orcamento = novoOrcamento();
            lancarCusto(adicionarItemDeMil(orcamento), "900");
            registrarDecisaoComId(orcamento, "100");
            assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());

            orcamento.editarDecisao(1L, "Contenção", "Corte de verba", new BigDecimal("-50"));

            assertEquals(new BigDecimal("950.00"), orcamento.calcularTotalOrcado());
            assertEquals(StatusOrcamento.ATENCAO, orcamento.getStatus());
            assertEquals("Contenção", orcamento.buscarDecisao(1L).getDescricao());
        }

        @Test
        void lancaExcecaoQuandoEdicaoDeixaOrcadoNegativo() {
            Orcamento orcamento = novoOrcamento();
            adicionarItemDeMil(orcamento);
            registrarDecisaoComId(orcamento, "100");

            assertThrows(RegraNegocioException.class, () -> orcamento.editarDecisao(1L, "Corte", "Corte",
                    new BigDecimal("-1001")));
            assertEquals(new BigDecimal("100.00"), orcamento.buscarDecisao(1L).getValorImpacto());
        }

        @Test
        void lancaExcecaoAoEditarDecisaoDeOrcamentoCancelado() {
            Orcamento orcamento = novoOrcamento();
            registrarDecisaoComId(orcamento, "100");
            orcamento.cancelar();

            assertThrows(RegraNegocioException.class, () -> orcamento.editarDecisao(1L, "D", "J", BigDecimal.ONE));
        }

        @Test
        void lancaExcecaoQuandoDecisaoNaoExiste() {
            assertThrows(RecursoNaoEncontradoException.class, () -> novoOrcamento().editarDecisao(9L, "D", "J",
                    BigDecimal.ONE));
        }
    }

    @Test
    void totaisSomamItensDecisoesECustos() {
        Orcamento orcamento = novoOrcamento();
        lancarCusto(adicionarItemDeMil(orcamento), "1100");
        orcamento.registrarDecisao("Aditivo", "Reajuste", new BigDecimal("250.50"), USUARIO_ID);

        IndicadorSaudeFinanceira indicador = orcamento.gerarIndicadoresSaude();

        assertEquals(new BigDecimal("1250.50"), indicador.getTotalOrcado());
        assertEquals(new BigDecimal("1100.00"), indicador.getTotalRealizado());
        assertEquals(new BigDecimal("-150.50"), indicador.getDesvioAbsoluto());
        assertEquals(1, orcamento.listarItensEmDesvio().size());
    }
}
