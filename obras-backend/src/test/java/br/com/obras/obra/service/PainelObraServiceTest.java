package br.com.obras.obra.service;

import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.DiarioResumoDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.MembroDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.service.DiarioService;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelObraServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);
    private static final long OBRA_ID = 7L;

    @Mock private ObraService obras;
    @Mock private DiarioService diarios;
    private PainelObraService service;

    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(HOJE.atTime(12, 0).atZone(ZONA).toInstant(), ZONA);
        service = new PainelObraService(obras, diarios, clock);
    }

    // 1
    @Test
    void painelIncluiObraEEquipeAtiva() {
        ObraDto obra = obra();
        List<EquipeMembroDto> equipe = equipe(2);
        when(obras.buscar(OBRA_ID)).thenReturn(obra);
        when(obras.equipe(OBRA_ID)).thenReturn(equipe);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of());

        PainelObraDto dto = service.painel(OBRA_ID);

        assertAll(
            () -> assertEquals(obra, dto.obra()),
            () -> assertEquals(equipe, dto.equipe()),
            () -> assertEquals(2, dto.maoDeObra().alocados())
        );
    }

    // 2
    @Test
    void painelSemDiarioHojeIndicaAusencia() {
        base(4);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of(resumo(8, HOJE.minusDays(1))));

        PainelObraDto dto = service.painel(OBRA_ID);

        assertAll(
            () -> assertNotNull(dto.clima()),
            () -> assertNull(dto.clima().data()),
            () -> assertNull(dto.clima().condicao()),
            () -> assertEquals(0, dto.maoDeObra().presentesHoje()),
            () -> assertNull(dto.maoDeObra().vsOntem()),
            () -> assertEquals(4, dto.maoDeObra().alocados()),
            () -> assertEquals("DIARIO", dto.maoDeObra().fonte()),
            () -> assertEquals(List.of(), dto.ocorrencias()),
            () -> assertEquals(List.of(), dto.fotos())
        );
        verify(diarios, never()).buscar(any(), any());
    }

    // 3 (hoje sem anterior -> vsOntem null)
    @Test
    void painelComDiarioUsaClimaEPresentes() {
        base(4);
        List<OcorrenciaDto> ocorrencias = List.of(
            ocorrencia(1, 10, List.of(foto(1, 1), foto(2, 1))),
            ocorrencia(2, 10, List.of())
        );
        when(diarios.listar(OBRA_ID)).thenReturn(List.of(resumo(10, HOJE)));
        when(diarios.buscar(OBRA_ID, 10L)).thenReturn(diario(10, HOJE, 3, ocorrencias));

        PainelObraDto dto = service.painel(OBRA_ID);

        assertAll(
            () -> assertEquals(HOJE, dto.clima().data()),
            () -> assertEquals("ENSOLARADO", dto.clima().condicao()),
            () -> assertEquals(3, dto.maoDeObra().presentesHoje()),
            () -> assertEquals(4, dto.maoDeObra().alocados()),
            () -> assertNull(dto.maoDeObra().vsOntem()),
            () -> assertEquals(2, dto.ocorrencias().size()),
            () -> assertEquals(List.of(1L, 2L), dto.fotos().stream().map(FotoDto::id).toList())
        );
    }

    // 4 (lacuna D-1 -> D-2; ignora futuro e D-3)
    @Test
    void vsOntemComparaDiarioAnterior() {
        base(6);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of(
            resumo(99, HOJE.plusDays(1)),
            resumo(10, HOJE),
            resumo(8, HOJE.minusDays(2)),
            resumo(7, HOJE.minusDays(3))
        ));
        when(diarios.buscar(OBRA_ID, 10L)).thenReturn(diario(10, HOJE, 5, List.of()));
        when(diarios.buscar(OBRA_ID, 8L)).thenReturn(diario(8, HOJE.minusDays(2), 2, List.of()));

        PainelObraDto dto = service.painel(OBRA_ID);

        assertEquals(3, dto.maoDeObra().vsOntem());
        verify(diarios, never()).buscar(OBRA_ID, 7L);
        verify(diarios, never()).buscar(OBRA_ID, 99L);
    }

    // 5
    @Test
    void kpisSemModuloTemFonteSimulado() {
        base(1);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of());

        PainelObraDto.KpisDto k = service.painel(OBRA_ID).kpis();

        assertAll(
            () -> assertNull(k.avancoRealizado()),
            () -> assertNull(k.avancoPrevisto()),
            () -> assertNull(k.spi()),
            () -> assertNull(k.orcamentoTotal()),
            () -> assertNull(k.executado()),
            () -> assertNull(k.limiteAlerta()),
            () -> assertEquals("SIMULADO", k.fonte())
        );
    }

    // 8
    @Test
    void painelObraInexistenteLancaRecursoNaoEncontrado() {
        when(obras.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Obra", 99L));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.painel(99L));

        verify(obras, never()).equipe(any());
        verifyNoInteractions(diarios);
    }

    // 9 (diário sem equipe e sem ocorrências -> listas vazias, não null)
    @Test
    void painelSemEquipePresenteNoDiarioNaoInventaMaoDeObra() {
        base(4);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of(resumo(10, HOJE)));
        when(diarios.buscar(OBRA_ID, 10L)).thenReturn(diario(10, HOJE, 0, List.of()));

        PainelObraDto dto = service.painel(OBRA_ID);

        assertAll(
            () -> assertEquals(0, dto.maoDeObra().presentesHoje()),
            () -> assertEquals(4, dto.maoDeObra().alocados()),
            () -> assertNull(dto.maoDeObra().vsOntem()),
            () -> assertEquals(List.of(), dto.ocorrencias()),
            () -> assertEquals(List.of(), dto.fotos())
        );
    }

    // 11 (espelha L42-api-painel.md:18-31: alocados 4, presentes 3, vsOntem 1)
    @Test
    void painelDtoTemCamposObrigatoriosDoContrato() {
        base(4);
        when(diarios.listar(OBRA_ID)).thenReturn(List.of(resumo(10, HOJE), resumo(8, HOJE.minusDays(1))));
        when(diarios.buscar(OBRA_ID, 10L)).thenReturn(diario(10, HOJE, 3, List.of()));
        when(diarios.buscar(OBRA_ID, 8L)).thenReturn(diario(8, HOJE.minusDays(1), 2, List.of()));

        PainelObraDto dto = service.painel(OBRA_ID);
        ObjectMapper json = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonNode n = json.valueToTree(dto);

        assertAll(
            () -> assertEquals(Set.of("obra", "clima", "kpis", "equipe", "maoDeObra", "ocorrencias", "fotos"), nomes(n)),
            () -> assertEquals(Set.of("data", "condicao"), nomes(n.get("clima"))),
            () -> assertEquals(Set.of("avancoRealizado", "avancoPrevisto", "spi", "orcamentoTotal",
                    "executado", "limiteAlerta", "fonte"), nomes(n.get("kpis"))),
            () -> assertEquals(Set.of("alocados", "presentesHoje", "vsOntem", "fonte"), nomes(n.get("maoDeObra"))),
            () -> assertEquals("2026-11-26", n.get("clima").get("data").asText()),
            () -> assertTrue(n.get("kpis").get("spi").isNull()),
            () -> assertEquals("SIMULADO", n.get("kpis").get("fonte").asText()),
            () -> assertEquals(4, n.get("maoDeObra").get("alocados").asInt()),
            () -> assertEquals(3, n.get("maoDeObra").get("presentesHoje").asInt()),
            () -> assertEquals(1, n.get("maoDeObra").get("vsOntem").asInt()),
            () -> assertEquals("DIARIO", n.get("maoDeObra").get("fonte").asText()),
            () -> assertTrue(n.get("ocorrencias").isArray()),
            () -> assertTrue(n.get("fotos").isArray())
        );
    }

    // ---- helpers ----
    private void base(int alocados) {
        when(obras.buscar(OBRA_ID)).thenReturn(obra());
        when(obras.equipe(OBRA_ID)).thenReturn(equipe(alocados));
    }

    private static Set<String> nomes(JsonNode no) {
        Set<String> r = new java.util.HashSet<>();
        no.fieldNames().forEachRemaining(r::add);
        return r;
    }

    private static ObraDto obra() {
        return new ObraDto(OBRA_ID, "Obra A", "desc", "RESIDENCIAL", "Rua X",
            HOJE.minusDays(30), HOJE.plusDays(90), "EM_ANDAMENTO", "ART-1", 1L);
    }

    private static List<EquipeMembroDto> equipe(int n) {
        return IntStream.rangeClosed(1, n)
            .mapToObj(i -> new EquipeMembroDto((long) i, "Membro " + i, List.of("Pedreiro"), HOJE.minusDays(10)))
            .toList();
    }

    private static DiarioResumoDto resumo(long id, LocalDate data) {
        return new DiarioResumoDto(id, data, "ENSOLARADO", "d", 0L);
    }

    private static DiarioDto diario(long id, LocalDate data, int presentes, List<OcorrenciaDto> ocorrencias) {
        return new DiarioDto(id, OBRA_ID, data, "ENSOLARADO", "d",
            Collections.nCopies(presentes, mock(MembroDiarioDto.class)), ocorrencias);
    }

    private static OcorrenciaDto ocorrencia(long id, long diarioId, List<FotoDto> fotos) {
        return new OcorrenciaDto(id, diarioId, "OUTRO", "desc", HOJE.atTime(9, 0), null, null, fotos);
    }

    private static FotoDto foto(long id, long ocorrenciaId) {
        return new FotoDto(id, ocorrenciaId, "f" + id + ".jpg", "legenda", HOJE.atTime(9, 5));
    }
}