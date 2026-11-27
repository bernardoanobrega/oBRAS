package br.com.obras.obra.domain;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.domain.RegistroAuditoria;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ObraTest {
    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    private static Auditavel auditavel(Long id) {
        Auditavel a = Auditavel.novo();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private static Obra obra(String nome, Tipologia tipologia, LocalDate inicio, LocalDate fim,
                             Long responsavelId, Auditavel auditavel) {
        return Obra.cadastrar(nome, null, tipologia, "Rua teste", inicio, fim, null, responsavelId, auditavel);
    }

    private static Obra obraComResponsavel() {
        return obra("Obra teste", Tipologia.RESIDENCIAL, INICIO, FIM, 10L, auditavel(1L));
    }

    private static Obra obraSemResponsavel() {
        return obra("Obra teste", Tipologia.RESIDENCIAL, INICIO, FIM, null, auditavel(1L));
    }

    @Test
    void cadastraObraPlanejadaSemResponsavel() {
        Obra obra = assertDoesNotThrow(
                () -> obra("Obra teste", Tipologia.RESIDENCIAL, INICIO, FIM, null, auditavel(1L)));

        assertAll(
                () -> assertEquals(StatusObra.PLANEJADA, obra.getStatus()),
                () -> assertNull(obra.getResponsavelId()),
                () -> assertEquals("Obra teste", obra.getNome()),
                () -> assertEquals(Tipologia.RESIDENCIAL, obra.getTipologia())
        );
    }

    @Test
    void rejeitaPeriodoInvertido() {
        assertThrows(RegraNegocioException.class,
                () -> obra("Obra teste", Tipologia.RESIDENCIAL, FIM, INICIO, null, auditavel(1L)));
    }

    @Test
    void rejeitaNomeVazio() {
        assertAll(
                () -> assertThrows(RegraNegocioException.class,
                        () -> obra(null, Tipologia.RESIDENCIAL, INICIO, FIM, null, auditavel(1L))),
                () -> assertThrows(RegraNegocioException.class,
                        () -> obra("   ", Tipologia.RESIDENCIAL, INICIO, FIM, null, auditavel(1L)))
        );
    }

    @Test
    void rejeitaTipologiaNula() {
        assertThrows(RegraNegocioException.class,
                () -> obra("Obra teste", null, INICIO, FIM, null, auditavel(1L)));
    }

    @Test
    void obraCriadaTemAuditavelNaoExcluido() {
        Auditavel auditavel = Auditavel.novo();
        ReflectionTestUtils.setField(auditavel, "id", 99L);

        Obra obra = obra("Obra teste", Tipologia.RESIDENCIAL, INICIO, FIM, null, auditavel);

        assertAll(
                () -> assertEquals(StatusObra.PLANEJADA, obra.getStatus()),
                () -> assertEquals(99L, obra.getAuditavelId()),
                () -> assertNotNull(obra.getAuditavel()),
                () -> assertFalse(obra.getAuditavel().isExcluido())
        );
    }

    @Test
    void naoIniciaSemResponsavel() {
        Obra obra = obraSemResponsavel();

        assertThrows(RegraNegocioException.class, () -> obra.iniciar(true, 1L));
    }

    @Test
    void naoIniciaComResponsavelInativo() {
        Obra obra = obraComResponsavel();

        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> obra.iniciar(false, 1L)),
                () -> assertEquals(StatusObra.PLANEJADA, obra.getStatus())
        );
    }

    @Test
    void iniciaComResponsavelAtivo() {
        Obra obra = obraComResponsavel();

        assertDoesNotThrow(() -> obra.iniciar(true, 1L));

        assertEquals(StatusObra.EM_ANDAMENTO, obra.getStatus());
    }

    @Test
    void paralisaRetomaConclui() {
        Obra obra = obraComResponsavel();

        obra.iniciar(true, 1L);
        assertEquals(StatusObra.EM_ANDAMENTO, obra.getStatus());

        obra.paralisar(1L);
        assertEquals(StatusObra.PARALISADA, obra.getStatus());

        obra.retomar(1L);
        assertEquals(StatusObra.EM_ANDAMENTO, obra.getStatus());

        obra.concluir(1L);
        assertEquals(StatusObra.CONCLUIDA, obra.getStatus());
    }

    @Test
    void cancelaAPartirDePlanejadaEmAndamentoEParalisada() {
        Obra planejada = obraComResponsavel();
        Obra emAndamento = obraComResponsavel();
        Obra paralisada = obraComResponsavel();

        emAndamento.iniciar(true, 1L);
        paralisada.iniciar(true, 1L);
        paralisada.paralisar(1L);

        planejada.cancelar(1L);
        emAndamento.cancelar(1L);
        paralisada.cancelar(1L);

        assertAll(
                () -> assertEquals(StatusObra.CANCELADA, planejada.getStatus()),
                () -> assertEquals(StatusObra.CANCELADA, emAndamento.getStatus()),
                () -> assertEquals(StatusObra.CANCELADA, paralisada.getStatus())
        );
    }

    @Test
    void naoAlteraObraConcluidaOuCancelada() {
        Obra concluida = obraComResponsavel();
        concluida.iniciar(true, 1L);
        concluida.concluir(1L);

        Obra cancelada = obraComResponsavel();
        cancelada.cancelar(1L);

        for (Obra obra : new Obra[]{concluida, cancelada}) {
            assertAll(
                    () -> assertThrows(RegraNegocioException.class, () -> obra.iniciar(true, 1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.paralisar(1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.retomar(1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.concluir(1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.cancelar(1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.definirResponsavel(20L, 1L)),
                    () -> assertThrows(RegraNegocioException.class, () -> obra.atualizarParametros(
                            "Novo nome", null, Tipologia.COMERCIAL, "Rua nova", INICIO, FIM, null, 1L))
            );
        }
    }

    @Test
    void transicaoGeraAuditoriaComStatusAnteriorENovo() {
        Obra obra = obraComResponsavel();

        RegistroAuditoria r = obra.iniciar(true, 55L);

        assertAll(
                () -> assertEquals(AcaoAuditoria.EDICAO, r.getAcao()),
                () -> assertEquals(55L, r.getUsuarioId()),
                () -> assertTrue(r.getDetalhe().contains("\"statusAnterior\":\"PLANEJADA\"")),
                () -> assertTrue(r.getDetalhe().contains("\"statusNovo\":\"EM_ANDAMENTO\""))
        );
    }

    @Test
    void definirResponsavelGeraAuditoriaComIds() {
        Obra obra = obraComResponsavel();

        RegistroAuditoria r = obra.definirResponsavel(20L, 55L);

        assertAll(
                () -> assertEquals(20L, obra.getResponsavelId()),
                () -> assertEquals(AcaoAuditoria.EDICAO, r.getAcao()),
                () -> assertEquals(55L, r.getUsuarioId()),
                () -> assertTrue(r.getDetalhe().contains("\"responsavelAnterior\":10")),
                () -> assertTrue(r.getDetalhe().contains("\"responsavelNovo\":20"))
        );
    }

    @Test
    void atualizarParametrosGeraAuditoriaDeEdicao() {
        Obra obra = obraComResponsavel();

        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> obra.atualizarParametros(
                        "Outro nome", null, Tipologia.COMERCIAL, "Rua nova", FIM, INICIO, null, 1L)),
                () -> assertEquals("Obra teste", obra.getNome())
        );

        RegistroAuditoria r = obra.atualizarParametros(
                "Outro nome", "nova desc", Tipologia.COMERCIAL, "Rua nova", INICIO, FIM, "ART-9", 55L);

        assertAll(
                () -> assertEquals("Outro nome", obra.getNome()),
                () -> assertEquals("nova desc", obra.getDescricao()),
                () -> assertEquals(Tipologia.COMERCIAL, obra.getTipologia()),
                () -> assertEquals("Rua nova", obra.getEndereco()),
                () -> assertEquals("ART-9", obra.getArt()),
                () -> assertEquals(StatusObra.PLANEJADA, obra.getStatus()),
                () -> assertEquals(AcaoAuditoria.EDICAO, r.getAcao()),
                () -> assertEquals(55L, r.getUsuarioId()),
                () -> assertTrue(r.getDetalhe().contains("\"campos\":\"parametros\""))
        );
    }
}
