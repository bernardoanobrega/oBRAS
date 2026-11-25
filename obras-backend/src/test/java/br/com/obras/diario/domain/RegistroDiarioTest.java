package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RegistroDiarioTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 24);

    private final Obra obra = mock(Obra.class);
    private final AlocacaoObra ana = mock(AlocacaoObra.class);
    private final AlocacaoObra bruno = mock(AlocacaoObra.class);

    @Test
    void criaDiarioComClimaUnico() {
        RegistroDiario d = RegistroDiario.criar(
            Auditavel.novo(), obra, DIA, Clima.NUBLADO, "Concretagem da laje", List.of(ana)
        );

        assertAll(
            () -> assertEquals(DIA, d.getData()),
            () -> assertEquals(Clima.NUBLADO, d.getClima()),
            () -> assertEquals("Concretagem da laje", d.getDescricao()),
            () -> assertSame(obra, d.getObra()),
            () -> assertFalse(d.isExcluido()),
            () -> assertEquals(1, d.getEquipePresente().size())
        );
    }

    @Test
    void rejeitaClimaNulo() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            RegistroDiario.criar(Auditavel.novo(), obra, DIA, null, null, List.of())
        );
        assertEquals("Clima do diário é obrigatório", e.getMessage());
    }

    @Test
    void rejeitaDataNula() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            RegistroDiario.criar(Auditavel.novo(), obra, null, Clima.ENSOLARADO, null, List.of())
        );
        assertEquals("Data do diário é obrigatória", e.getMessage());
    }

    @Test
    void atualizaDescricaoEClimaSemAlterarData() {
        RegistroDiario d = RegistroDiario.criar(
            Auditavel.novo(), obra, DIA, Clima.ENSOLARADO, "Manhã seca", List.of(ana)
        );

        d.editar(Clima.CHUVA_FORTE, "Chuva à tarde", List.of(bruno));

        assertAll(
            () -> assertEquals(Clima.CHUVA_FORTE, d.getClima()),
            () -> assertEquals("Chuva à tarde", d.getDescricao()),
            () -> assertEquals(DIA, d.getData()),
            () -> assertTrue(d.getEquipePresente().contains(bruno)),
            () -> assertFalse(d.getEquipePresente().contains(ana))
        );
    }

    @Test
    void editarRejeitaClimaNuloEMantemEstado() {
        RegistroDiario d = RegistroDiario.criar(
            Auditavel.novo(), obra, DIA, Clima.ENSOLARADO, "Manhã seca", List.of(ana)
        );

        assertThrows(RegraNegocioException.class, () -> d.editar(null, "Outra", List.of()));

        assertAll(
            () -> assertEquals(Clima.ENSOLARADO, d.getClima()),
            () -> assertEquals("Manhã seca", d.getDescricao()),
            () -> assertTrue(d.getEquipePresente().contains(ana))
        );
    }

    @Test
    void equipePresenteNaoAceitaDuplicata() {
        AlocacaoObra carla = new AlocacaoObra();
        AlocacaoObra carlaOutraInstancia = new AlocacaoObra();
        ReflectionTestUtils.setField(carla, "id", 30L);
        ReflectionTestUtils.setField(carlaOutraInstancia, "id", 30L);

        RegistroDiario d = RegistroDiario.criar(
            Auditavel.novo(), obra, DIA, Clima.NUBLADO, null, List.of(carla, carlaOutraInstancia)
        );

        assertEquals(1, d.getEquipePresente().size());
    }

    @Test
    void exclusaoLogicaMarcaExcluidoSemApagarDados() {
        RegistroDiario d = RegistroDiario.criar(
            Auditavel.novo(), obra, DIA, Clima.NUBLADO, "Dia normal", List.of(ana)
        );

        d.excluirLogico();

        assertAll(
            () -> assertTrue(d.isExcluido()),
            () -> assertEquals(Clima.NUBLADO, d.getClima()),
            () -> assertEquals(DIA, d.getData())
        );
    }
}
