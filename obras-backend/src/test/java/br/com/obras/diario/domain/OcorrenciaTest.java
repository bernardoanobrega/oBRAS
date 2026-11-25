package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OcorrenciaTest {
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 24, 9, 30);

    private final RegistroDiario diario = mock(RegistroDiario.class);
    private final AlocacaoObra autor = mock(AlocacaoObra.class);

    {
        when(diario.getData()).thenReturn(AGORA.toLocalDate()); // diário do dia 24/11
    }

    @Test
    void criaOcorrenciaComDiarioECategoria() {
        Ocorrencia o = Ocorrencia.criar(
            Auditavel.novo(), diario, autor, AGORA, CategoriaOcorrencia.LOGISTICA, "Atraso do concreto"
        );

        assertAll(
            () -> assertSame(diario, o.getRegistroDiario()),
            () -> assertSame(autor, o.getAlocacaoObra()),
            () -> assertEquals(CategoriaOcorrencia.LOGISTICA, o.getCategoria()),
            () -> assertEquals("Atraso do concreto", o.getDescricao()),
            () -> assertEquals(AGORA, o.getDataHora()),
            () -> assertNull(o.getParalisacao()),
            () -> assertTrue(o.getFotos().isEmpty())
        );
    }

    @Test
    void usaAgoraQuandoDataHoraNaoInformada() {
        when(diario.getData()).thenReturn(LocalDate.now()); // diário de hoje
        LocalDateTime antes = LocalDateTime.now();
        Ocorrencia o = Ocorrencia.criar(
            Auditavel.novo(), diario, autor, null, CategoriaOcorrencia.OUTRO, "Sem hora"
        );
        assertFalse(o.getDataHora().isBefore(antes));
    }

    @Test
    void rejeitaOcorrenciaSemDiario() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            Ocorrencia.criar(Auditavel.novo(), null, autor, AGORA, CategoriaOcorrencia.OUTRO, "Texto")
        );
        assertEquals("Ocorrência precisa estar vinculada a um diário", e.getMessage());
    }

    @Test
    void rejeitaOcorrenciaSemAutor() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            Ocorrencia.criar(Auditavel.novo(), diario, null, AGORA, CategoriaOcorrencia.OUTRO, "Texto")
        );
        assertEquals("Autor da ocorrência é obrigatório", e.getMessage());
    }

    @Test
    void rejeitaOcorrenciaSemCategoria() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class, () ->
            Ocorrencia.criar(Auditavel.novo(), diario, autor, AGORA, null, "Texto")
        );
        assertEquals("Categoria da ocorrência é obrigatória", e.getMessage());
    }

    @Test
    void editarSubstituiCategoriaEDescricao() {
        Ocorrencia o = novaOcorrencia();

        o.editar(null, CategoriaOcorrencia.SEGURANCA, "Andaime sem guarda-corpo");

        assertAll(
            () -> assertEquals(CategoriaOcorrencia.SEGURANCA, o.getCategoria()),
            () -> assertEquals("Andaime sem guarda-corpo", o.getDescricao()),
            () -> assertEquals(AGORA, o.getDataHora())
        );
    }

    @Test
    void editarRejeitaCategoriaNulaEMantemEstado() {
        Ocorrencia o = novaOcorrencia();

        assertThrows(RegraNegocioException.class, () -> o.editar(null, null, "Outra descrição"));

        assertAll(
            () -> assertEquals(CategoriaOcorrencia.LOGISTICA, o.getCategoria()),
            () -> assertEquals("Atraso do concreto", o.getDescricao())
        );
    }

    @Test
    void editarRejeitaDescricaoVaziaEMantemEstado() {
        Ocorrencia o = novaOcorrencia();

        assertThrows(RegraNegocioException.class, () ->
            o.editar(AGORA.plusHours(1), CategoriaOcorrencia.OUTRO, "  ")
        );

        assertAll(
            () -> assertEquals(CategoriaOcorrencia.LOGISTICA, o.getCategoria()),
            () -> assertEquals("Atraso do concreto", o.getDescricao()),
            () -> assertEquals(AGORA, o.getDataHora())
        );
    }

    private Ocorrencia novaOcorrencia() {
        return Ocorrencia.criar(
            Auditavel.novo(), diario, autor, AGORA, CategoriaOcorrencia.LOGISTICA, "Atraso do concreto"
        );
    }

    @Test
    void rejeitaOcorrenciaSemDescricao() {
        assertAll(
            () -> assertThrows(RegraNegocioException.class, () ->
                Ocorrencia.criar(Auditavel.novo(), diario, autor, AGORA, CategoriaOcorrencia.OUTRO, null)),
            () -> assertThrows(RegraNegocioException.class, () ->
                Ocorrencia.criar(Auditavel.novo(), diario, autor, AGORA, CategoriaOcorrencia.OUTRO, "   "))
        );
    }

    // ---------- ADR-18: ocorrência no mesmo dia do diário ----------

    @Test
    void rejeitaOcorrenciaDeOutroDia() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> Ocorrencia.criar(
            Auditavel.novo(), diario, autor, AGORA.plusDays(1), CategoriaOcorrencia.OUTRO, "Dia seguinte"
        ));
        assertEquals("A ocorrência deve ser do mesmo dia do diário", erro.getMessage());
    }

    @Test
    void aceitaQualquerHorarioDoMesmoDia() {
        assertAll(
            () -> assertDoesNotThrow(() -> Ocorrencia.criar(Auditavel.novo(), diario, autor,
                AGORA.toLocalDate().atStartOfDay(), CategoriaOcorrencia.OUTRO, "Meia-noite")),
            () -> assertDoesNotThrow(() -> Ocorrencia.criar(Auditavel.novo(), diario, autor,
                AGORA.toLocalDate().atTime(23, 59, 59), CategoriaOcorrencia.OUTRO, "Fim do dia"))
        );
    }

    @Test
    void semDataHoraEmDiarioDeDiaPassadoERecusada() {
        when(diario.getData()).thenReturn(LocalDate.now().minusDays(3));

        assertThrows(RegraNegocioException.class, () -> Ocorrencia.criar(
            Auditavel.novo(), diario, autor, null, CategoriaOcorrencia.OUTRO, "Registro atrasado sem horário"
        ), "sem horário vale agora, que é de outro dia: quem registra atrasado informa o horário");
    }

    @Test
    void editarRejeitaDataDeOutroDiaEMantemEstado() {
        Ocorrencia o = novaOcorrencia();

        assertThrows(RegraNegocioException.class, () ->
            o.editar(AGORA.minusDays(1), CategoriaOcorrencia.SEGURANCA, "Mudou de dia")
        );

        assertAll(
            () -> assertEquals(AGORA, o.getDataHora()),
            () -> assertEquals(CategoriaOcorrencia.LOGISTICA, o.getCategoria())
        );
    }
}
