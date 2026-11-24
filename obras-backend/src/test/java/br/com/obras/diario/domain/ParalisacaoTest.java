package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ParalisacaoTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 24, 13, 0);

    private final Ocorrencia ocorrencia = Ocorrencia.criar(
        Auditavel.novo(), mock(RegistroDiario.class), mock(AlocacaoObra.class),
        INICIO, CategoriaOcorrencia.INTEMPERIE, "Chuva forte parou a concretagem"
    );

    @Test
    void criaParalisacaoComCategoria() {
        Paralisacao p = Paralisacao.criar(ocorrencia, INICIO, INICIO.plusHours(2));
        ocorrencia.definirParalisacao(p);

        assertAll(
            () -> assertSame(ocorrencia, p.getOcorrencia()),
            () -> assertEquals(CategoriaOcorrencia.INTEMPERIE, p.getOcorrencia().getCategoria()),
            () -> assertEquals(INICIO, p.getInicio()),
            () -> assertEquals(INICIO.plusHours(2), p.getFim()),
            () -> assertSame(p, ocorrencia.getParalisacao())
        );
    }

    @Test
    void aceitaParalisacaoSemFim() {
        Paralisacao p = Paralisacao.criar(ocorrencia, INICIO, null);
        assertNull(p.getFim());
    }

    @Test
    void aceitaFimIgualAoInicio() {
        assertDoesNotThrow(() -> Paralisacao.criar(ocorrencia, INICIO, INICIO));
    }

    @Test
    void rejeitaParalisacaoComFimMenorQueInicio() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            Paralisacao.criar(ocorrencia, INICIO, INICIO.minusMinutes(1))
        );
        assertEquals("Fim da paralisação não pode ser antes do início", e.getMessage());
    }

    @Test
    void rejeitaParalisacaoSemInicio() {
        assertThrows(RegraNegocioException.class, () -> Paralisacao.criar(ocorrencia, null, null));
    }

    @Test
    void rejeitaParalisacaoSemOcorrencia() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            Paralisacao.criar(null, INICIO, null)
        );
        assertEquals("Paralisação precisa estar vinculada a uma ocorrência", e.getMessage());
    }

    @Test
    void editarRejeitaPeriodoInvalidoEMantemEstado() {
        Paralisacao p = Paralisacao.criar(ocorrencia, INICIO, INICIO.plusHours(1));

        assertThrows(RegraNegocioException.class, () -> p.editar(INICIO, INICIO.minusHours(1)));

        assertAll(
            () -> assertEquals(INICIO, p.getInicio()),
            () -> assertEquals(INICIO.plusHours(1), p.getFim())
        );
    }
}
