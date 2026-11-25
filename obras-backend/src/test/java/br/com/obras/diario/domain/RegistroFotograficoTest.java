package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RegistroFotograficoTest {
    private final Ocorrencia ocorrencia = mock(Ocorrencia.class);

    @Test
    void criaFotoVinculadaAOcorrencia() {
        LocalDateTime antes = LocalDateTime.now();
        RegistroFotografico f = RegistroFotografico.criar(
            Auditavel.novo(), ocorrencia, "2026/11/fachada.jpg", "Fachada leste"
        );

        assertAll(
            () -> assertSame(ocorrencia, f.getOcorrencia()),
            () -> assertEquals("2026/11/fachada.jpg", f.getArquivo()),
            () -> assertEquals("Fachada leste", f.getLegenda()),
            () -> assertFalse(f.getDataEnvio().isBefore(antes))
        );
    }

    @Test
    void rejeitaFotoSemOcorrencia() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            RegistroFotografico.criar(Auditavel.novo(), null, "2026/11/avulsa.jpg", null)
        );
        assertEquals("Foto precisa estar vinculada a uma ocorrência", e.getMessage());
    }

    @Test
    void rejeitaFotoSemArquivo() {
        assertThrows(RegraNegocioException.class, () ->
            RegistroFotografico.criar(Auditavel.novo(), ocorrencia, " ", "Sem arquivo")
        );
    }
}
