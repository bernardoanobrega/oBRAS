package br.com.obras.obra.domain;

import br.com.obras.auditoria.domain.Auditavel;
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
}
