package br.com.obras.obra.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObraHidratacaoAuditavelTest {
    @Test
    void hidrataAuditavelComMesmoId() {
        Auditavel origem = auditavel(91L, false);
        Obra obra = Obra.cadastrar("Obra", null, Tipologia.RESIDENCIAL, "Rua A",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, null, origem);

        Auditavel carregado = auditavel(91L, false);
        obra.hidratarAuditavel(carregado);

        assertSame(carregado, obra.getAuditavel());
    }

    @Test
    void rejeitaAuditavelDeOutroId() {
        Auditavel origem = auditavel(91L, false);
        Obra obra = Obra.cadastrar("Obra", null, Tipologia.RESIDENCIAL, "Rua A",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, null, origem);

        assertThrows(RegraNegocioException.class, () -> obra.hidratarAuditavel(auditavel(92L, false)));
    }

    @Test
    void rejeitaAuditavelExcluido() {
        Auditavel origem = auditavel(91L, false);
        Obra obra = Obra.cadastrar("Obra", null, Tipologia.RESIDENCIAL, "Rua A",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, null, origem);

        assertThrows(RegraNegocioException.class, () -> obra.hidratarAuditavel(auditavel(91L, true)));
    }

    private static Auditavel auditavel(Long id, boolean excluido) {
        Auditavel auditavel = mock(Auditavel.class);
        when(auditavel.getId()).thenReturn(id);
        when(auditavel.isExcluido()).thenReturn(excluido);
        return auditavel;
    }
}
