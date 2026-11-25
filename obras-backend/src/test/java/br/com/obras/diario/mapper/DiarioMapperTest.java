package br.com.obras.diario.mapper;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.Paralisacao;
import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.diario.domain.RegistroFotografico;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.MembroDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** O mapper só copia dados para os DTOs: sem banco, sem regra (ADR-16). */
class DiarioMapperTest {
    private static final LocalDateTime HORA = LocalDateTime.of(2026, 11, 18, 10, 0);

    private final DiarioMapper mapper = new DiarioMapper();
    private final Obra obra = obra(2L);
    private final AlocacaoObra bruno = alocacao(7L, 70L, "Bruno");
    private final AlocacaoObra ana = alocacao(9L, 90L, "ana");

    @Test
    void mapperDiarioIncluiClimaEEquipe() {
        RegistroDiario diario = diario(100L, Set.of(bruno, ana));

        DiarioDto dto = mapper.paraDto(diario, List.of(), List.of());

        assertAll(
                () -> assertEquals(100L, dto.id()),
                () -> assertEquals(2L, dto.obraId()),
                () -> assertEquals("CHUVA_FRACA", dto.clima()),
                () -> assertEquals(List.of(
                        new MembroDiarioDto(9L, 90L, "ana"),
                        new MembroDiarioDto(7L, 70L, "Bruno")
                ), dto.equipePresente(), "equipe com nomes, em ordem alfabética sem diferenciar maiúsculas")
        );
    }

    @Test
    void mapperOcorrenciaIncluiCategoriaEAutor() {
        Ocorrencia ocorrencia = ocorrencia(200L, diario(100L, Set.of()), bruno);

        OcorrenciaDto dto = mapper.paraDto(ocorrencia, List.of());

        assertAll(
                () -> assertEquals(200L, dto.id()),
                () -> assertEquals(100L, dto.diarioId()),
                () -> assertEquals("SEGURANCA", dto.categoria()),
                () -> assertEquals(new MembroDiarioDto(7L, 70L, "Bruno"), dto.autor()),
                () -> assertNull(dto.paralisacao(), "sem paralisação vira null, não objeto vazio"),
                () -> assertEquals(List.of(), dto.fotos())
        );
    }

    @Test
    void mapperDiarioDistribuiFotosEntreAsOcorrencias() {
        RegistroDiario diario = diario(100L, Set.of());
        Ocorrencia comFoto = ocorrencia(200L, diario, bruno);
        Ocorrencia semFoto = ocorrencia(201L, diario, ana);
        RegistroFotografico foto = foto(300L, comFoto, "obras/2/diario/abc-laje.jpg");

        DiarioDto dto = mapper.paraDto(diario, List.of(comFoto, semFoto), List.of(foto));

        assertAll(
                () -> assertEquals(List.of(300L), dto.ocorrencias().get(0).fotos().stream().map(f -> f.id()).toList()),
                () -> assertEquals(List.of(), dto.ocorrencias().get(1).fotos())
        );
    }

    @Test
    void mapperFotoExpoeSoONomeDoArquivo() {
        RegistroFotografico foto = foto(300L, ocorrencia(200L, diario(100L, Set.of()), bruno), "obras/2/diario/abc-laje.jpg");

        assertEquals("abc-laje.jpg", mapper.paraDto(foto).nomeArquivo(), "caminho interno do storage não vaza");
    }

    @Test
    void mapperParalisacaoCopiaInicioEFim() {
        Ocorrencia ocorrencia = ocorrencia(200L, diario(100L, Set.of()), bruno);
        Paralisacao p = Paralisacao.criar(ocorrencia, HORA, HORA.plusHours(2));

        var dto = mapper.paraDto(p);

        assertAll(
                () -> assertEquals(HORA, dto.inicio()),
                () -> assertEquals(HORA.plusHours(2), dto.fim())
        );
    }

    // ---------- montagem das entidades ----------

    private RegistroDiario diario(Long id, Set<AlocacaoObra> equipe) {
        RegistroDiario d = RegistroDiario.criar(auditavel(id), obra, LocalDate.of(2026, 11, 18),
                Clima.CHUVA_FRACA, "Concretagem", equipe);
        ReflectionTestUtils.setField(d, "id", id);
        return d;
    }

    private Ocorrencia ocorrencia(Long id, RegistroDiario diario, AlocacaoObra autor) {
        Ocorrencia o = Ocorrencia.criar(auditavel(id), diario, autor, HORA, CategoriaOcorrencia.SEGURANCA, "Sem EPI");
        ReflectionTestUtils.setField(o, "id", id);
        return o;
    }

    private RegistroFotografico foto(Long id, Ocorrencia ocorrencia, String arquivo) {
        RegistroFotografico f = RegistroFotografico.criar(auditavel(id), ocorrencia, arquivo, "Laje");
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }

    private static Auditavel auditavel(Long id) {
        Auditavel a = Auditavel.novo();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private static Obra obra(Long id) {
        Obra o = mock(Obra.class);
        when(o.getId()).thenReturn(id);
        return o;
    }

    private static AlocacaoObra alocacao(Long id, Long usuarioId, String nome) {
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn(usuarioId);
        when(u.getNome()).thenReturn(nome);
        AlocacaoObra a = mock(AlocacaoObra.class);
        when(a.getId()).thenReturn(id);
        when(a.getUsuario()).thenReturn(u);
        return a;
    }
}
