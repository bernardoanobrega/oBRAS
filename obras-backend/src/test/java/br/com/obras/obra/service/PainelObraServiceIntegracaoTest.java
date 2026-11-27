package br.com.obras.obra.service;

import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.diario.service.DiarioService;
import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.storage.ArquivoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PainelObraService.class, DiarioService.class, DiarioMapper.class, AuditoriaService.class,
        ObraService.class, ObraMapper.class, PainelObraServiceIntegracaoTest.RelogioFixo.class})
@Testcontainers
class PainelObraServiceIntegracaoTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 26);

    @TestConfiguration
    static class RelogioFixo {
        @Bean @Primary
        Clock clockFixo() {
            return Clock.fixed(HOJE.atTime(12, 0).atZone(ZONA).toInstant(), ZONA);
        }
    }

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private PainelObraService painelService;
    @Autowired private JdbcTemplate jdbc;
    @MockBean private ArquivoStorageService storage;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private long obraA;
    private long obraB;

    @BeforeEach
    void setup() {
        obraA = criarObra("Obra A");
        obraB = criarObra("Obra B");
    }

    // 6
    @Test
    void painelNaoVazaOcorrenciaExcluida() {
        long diario = criarDiario(obraA, HOJE);
        long viva = criarOcorrencia(diario);
        long excluida = criarOcorrencia(diario);
        criarFoto(excluida);
        excluirOcorrencia(excluida);

        PainelObraDto dto = painelService.painel(obraA);

        assertAll(
            () -> assertEquals(List.of(viva), ids(dto.ocorrencias())),   // controle: a viva aparece
            () -> assertTrue(dto.fotos().isEmpty())                       // foto da excluída não vaza
        );
    }

    // 7
    @Test
    void painelNaoVazaFotoDeOutraObra() {
        long ocA = criarOcorrencia(criarDiario(obraA, HOJE));
        long fotoA = criarFoto(ocA);
        long ocB = criarOcorrencia(criarDiario(obraB, HOJE));
        long fotoB = criarFoto(ocB);

        PainelObraDto dto = painelService.painel(obraA);

        List<Long> fotos = dto.fotos().stream().map(FotoDto::id).toList();
        assertAll(
            () -> assertTrue(fotos.contains(fotoA)),
            () -> assertFalse(fotos.contains(fotoB))
        );
    }

    // 10
    @Test
    void painelNaoVazaOcorrenciaDeOutraObra() {
        long ocA = criarOcorrencia(criarDiario(obraA, HOJE));
        long ocB = criarOcorrencia(criarDiario(obraB, HOJE));

        List<Long> ocs = ids(painelService.painel(obraA).ocorrencias());

        assertAll(
            () -> assertTrue(ocs.contains(ocA)),
            () -> assertFalse(ocs.contains(ocB))
        );
    }

    private static List<Long> ids(List<OcorrenciaDto> l) {
        return l.stream().map(OcorrenciaDto::id).toList();
    }

    // ---- FIXTURES ----

    private long criarObra(String nome) {
        long usuario = id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES (?, 'ATIVO', ? || gen_random_uuid(), 'hash') RETURNING id
                """, nome, nome);
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        long obra = id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, nome, usuario, auditavel);
        id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obra, usuario);
        return obra;
    }

    private long criarDiario(long obraId, LocalDate data) {
        return id("""
                INSERT INTO registro_diario (id, obra_id, data, clima)
                VALUES (?, ?, ?, 'ENSOLARADO') RETURNING id
                """, id("INSERT INTO auditavel DEFAULT VALUES RETURNING id"), obraId, data);
    }

    private long criarOcorrencia(long diarioId) {
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return id("""
                INSERT INTO ocorrencia (id, registro_diario_id, alocacao_obra_id, data_hora, categoria, descricao)
                VALUES (?, ?, (
                        SELECT a.id
                        FROM alocacao_obra a
                        JOIN registro_diario d ON d.obra_id = a.obra_id
                        WHERE d.id = ? AND a.data_fim IS NULL
                        ORDER BY a.id
                        LIMIT 1
                    ), TIMESTAMP '2026-11-26 10:00:00', 'OUTRO', 'Ocorrência de teste') RETURNING id
                """, auditavel, diarioId, diarioId);
    }

    private long criarFoto(long ocorrenciaId) {
        return id("""
                INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo)
                VALUES (?, ?, 'obras/1/foto.jpg') RETURNING id
                """, id("INSERT INTO auditavel DEFAULT VALUES RETURNING id"), ocorrenciaId);
    }

    private void excluirOcorrencia(long ocorrenciaId) {
        jdbc.update("UPDATE auditavel SET excluido = TRUE WHERE id = ?", ocorrenciaId);
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}