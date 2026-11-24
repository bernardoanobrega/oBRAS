package br.com.obras.diario;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class V4DiarioMigrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private String schema;
    private long usuarioId;
    private long obraId;
    private long alocacaoId;

    @Test
    void versaoV4Aplicada() throws SQLException {
        setup();
        assertTrue(versoesAplicadas().contains("4"));
    }

    @Test
    void tabelasEConstraintsExistem() throws SQLException {
        setup();

        assertAll(
                () -> assertTrue(tabelaExiste("registro_diario")),
                () -> assertTrue(tabelaExiste("registro_diario_equipe")),
                () -> assertTrue(tabelaExiste("ocorrencia")),
                () -> assertTrue(tabelaExiste("paralisacao")),
                () -> assertTrue(tabelaExiste("registro_fotografico")),
                () -> assertTrue(indiceExiste("uk_registro_diario_obra_data_ativo")),
                () -> assertTrue(constraintExiste("ck_reg_diario_clima", "CHECK")),
                () -> assertTrue(constraintExiste("ck_ocorrencia_categoria", "CHECK")),
                () -> assertTrue(constraintExiste("ck_paralisacao_periodo", "CHECK")),
                () -> assertEquals("auditavel", fkTarget("fk_registro_diario_auditavel")),
                () -> assertEquals("obra", fkTarget("fk_registro_diario_obra")),
                () -> assertEquals("registro_diario", fkTarget("fk_ocorrencia_registro_diario")),
                () -> assertEquals("ocorrencia", fkTarget("fk_paralisacao_ocorrencia")),
                () -> assertEquals("ocorrencia", fkTarget("fk_reg_fotografico_ocorrencia"))
        );
    }

    @Test
    void climaAceitaSomenteQuatroValores() throws SQLException {
        setupComDependencias();

        long diarioId = inserirDiario(obraId, "2026-11-18", "ENSOLARADO");
        assertTrue(diarioId > 0);

        assertViolacao("""
                INSERT INTO registro_diario (id, obra_id, data, clima)
                VALUES (%d, %d, DATE '2026-11-19', 'CHUVA_MODERADA')
                """.formatted(insertAuditavel(), obraId), "ck_reg_diario_clima");
    }

    @Test
    void ocorrenciaExigeDiario() throws SQLException {
        setupComDependencias();

        assertViolacao("""
                INSERT INTO ocorrencia (id, registro_diario_id, alocacao_obra_id, data_hora, categoria, descricao)
                VALUES (%d, NULL, %d, TIMESTAMP '2026-11-18 10:00:00', 'OUTRO', 'Sem diário')
                """.formatted(insertAuditavel(), alocacaoId), "registro_diario_id", "not-null");

        assertViolacao("""
                INSERT INTO ocorrencia (id, registro_diario_id, alocacao_obra_id, data_hora, categoria, descricao)
                VALUES (%d, %d, %d, TIMESTAMP '2026-11-18 10:00:00', 'OUTRO', 'Diário inválido')
                """.formatted(insertAuditavel(), Long.MAX_VALUE, alocacaoId), "fk_ocorrencia_registro_diario");
    }

    @Test
    void fotoExigeOcorrencia() throws SQLException {
        setupComDependencias();
        long diarioId = inserirDiario(obraId, "2026-11-18", "NUBLADO");
        long ocorrenciaId = inserirOcorrencia(diarioId, alocacaoId);

        assertViolacao("""
                INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo)
                VALUES (%d, NULL, 'foto.png')
                """.formatted(insertAuditavel()), "ocorrencia_id", "not-null");

        long fotoId = inserirFoto(ocorrenciaId);
        assertTrue(fotoId > 0);
    }

    @Test
    void umDiarioAtivoPorObraEData() throws SQLException {
        setupComDependencias();
        long primeiroDiarioId = inserirDiario(obraId, "2026-11-18", "ENSOLARADO");

        assertViolacao("""
                INSERT INTO registro_diario (id, obra_id, data, clima)
                VALUES (%d, %d, DATE '2026-11-18', 'CHUVA_FRACA')
                """.formatted(insertAuditavel(), obraId), "uk_registro_diario_obra_data_ativo");

        executar("UPDATE auditavel SET excluido = true WHERE id = " + primeiroDiarioId);
        assertTrue(excluidoEspelhado(primeiroDiarioId));

        long segundoDiarioId = inserirDiario(obraId, "2026-11-18", "CHUVA_FRACA");
        assertTrue(segundoDiarioId > 0);
        assertFalse(excluidoEspelhado(segundoDiarioId));
    }

    @Test
    void equipePresenteNaoAceitaDuplicata() throws SQLException {
        setupComDependencias();
        long diarioId = inserirDiario(obraId, "2026-11-18", "ENSOLARADO");
        String insert = """
                INSERT INTO registro_diario_equipe (registro_diario_id, alocacao_obra_id)
                VALUES (%d, %d)
                """.formatted(diarioId, alocacaoId);

        executar(insert);
        assertViolacao(insert, "pk_reg_diario_equipe");
    }

    @Test
    void paralisacaoRejeitaFimAntesDoInicio() throws SQLException {
        setupComDependencias();
        long diarioId = inserirDiario(obraId, "2026-11-18", "ENSOLARADO");
        long ocorrenciaId = inserirOcorrencia(diarioId, alocacaoId);

        assertViolacao("""
                INSERT INTO paralisacao (id, inicio_paralisacao, fim_paralisacao)
                VALUES (%d, TIMESTAMP '2026-11-18 10:00:00', TIMESTAMP '2026-11-18 09:00:00')
                """.formatted(ocorrenciaId), "ck_paralisacao_periodo");

        assertTrue(inserirParalisacao(ocorrenciaId) > 0);
    }

    @Test
    void migrationV4AplicaSobreV31ComDados() throws SQLException {
        schema = freshSchema();
        migrate("3.1");

        try (Connection conexao = connection()) {
            usuarioId = insertUsuario(conexao);
            obraId = insertObra(conexao, usuarioId);
        }
        assertEquals(List.of("1", "2", "3", "3.1"), versoesAplicadas());

        migrate(null);
        assertEquals(List.of("1", "2", "3", "3.1", "4"), versoesAplicadas());

        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("SELECT nome FROM obra WHERE id = ?")) {
            ps.setLong(1, obraId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("Obra V4", rs.getString(1));
            }
        }
    }

    private void setup() throws SQLException {
        schema = freshSchema();
        migrate(null);
    }

    private void setupComDependencias() throws SQLException {
        setup();
        try (Connection conexao = connection()) {
            usuarioId = insertUsuario(conexao);
            obraId = insertObra(conexao, usuarioId);
            alocacaoId = insertAlocacao(conexao, obraId, usuarioId);
        }
    }

    private void migrate(String target) {
        var config = Flyway.configure()
                .dataSource(jdbcUrl(schema), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .schemas(schema)
                .defaultSchema(schema);
        if (target != null) {
            config.target(MigrationVersion.fromVersion(target));
        }
        config.load().migrate();
    }

    private String freshSchema() throws SQLException {
        String nome = "v4_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection conexao = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = conexao.createStatement()) {
            st.executeUpdate("CREATE SCHEMA " + nome);
        }
        return nome;
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(schema), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private String jdbcUrl(String nomeSchema) {
        String base = POSTGRES.getJdbcUrl();
        return base + (base.contains("?") ? "&" : "?") + "currentSchema=" + nomeSchema;
    }

    private long insertAuditavel() throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private long insertUsuario(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Usuario V4', 'ATIVO', ?, 'hash') RETURNING id
                """)) {
            ps.setString(1, "v4-" + UUID.randomUUID());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertObra(Connection conexao, long responsavelId) throws SQLException {
        long auditavelId = insertAuditavel();
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra V4', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """)) {
            ps.setLong(1, responsavelId);
            ps.setLong(2, auditavelId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertAlocacao(Connection conexao, long obraId, long usuarioId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """)) {
            ps.setLong(1, obraId);
            ps.setLong(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long inserirDiario(long obraId, String data, String clima) throws SQLException {
        long auditavelId = insertAuditavel();
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     INSERT INTO registro_diario (id, obra_id, data, clima, descricao)
                     VALUES (?, ?, ?::date, ?, 'Descrição do diário')
                     """)) {
            ps.setLong(1, auditavelId);
            ps.setLong(2, obraId);
            ps.setString(3, data);
            ps.setString(4, clima);
            ps.executeUpdate();
        }
        return auditavelId;
    }

    private long inserirOcorrencia(long diarioId, long alocacaoId) throws SQLException {
        long auditavelId = insertAuditavel();
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     INSERT INTO ocorrencia (id, registro_diario_id, alocacao_obra_id,
                                             data_hora, categoria, descricao)
                     VALUES (?, ?, ?, TIMESTAMP '2026-11-18 10:00:00', 'OUTRO', 'Ocorrência do diário')
                     """)) {
            ps.setLong(1, auditavelId);
            ps.setLong(2, diarioId);
            ps.setLong(3, alocacaoId);
            ps.executeUpdate();
        }
        return auditavelId;
    }

    private long inserirFoto(long ocorrenciaId) throws SQLException {
        long auditavelId = insertAuditavel();
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo, legenda)
                     VALUES (?, ?, 'foto.png', 'Fachada leste')
                     """)) {
            ps.setLong(1, auditavelId);
            ps.setLong(2, ocorrenciaId);
            ps.executeUpdate();
        }
        return auditavelId;
    }

    private long inserirParalisacao(long ocorrenciaId) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     INSERT INTO paralisacao (id, inicio_paralisacao, fim_paralisacao)
                     VALUES (?, TIMESTAMP '2026-11-18 10:00:00', TIMESTAMP '2026-11-18 11:00:00')
                     """)) {
            ps.setLong(1, ocorrenciaId);
            ps.executeUpdate();
        }
        return ocorrenciaId;
    }

    private void executar(String sql) throws SQLException {
        try (Connection conexao = connection(); Statement st = conexao.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private boolean excluidoEspelhado(long diarioId) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement(
                     "SELECT excluido FROM registro_diario WHERE id = ?")) {
            ps.setLong(1, diarioId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBoolean(1);
            }
        }
    }

    private void assertViolacao(String sql, String... trechos) throws SQLException {
        SQLException erro = assertThrows(SQLException.class, () -> executar(sql));
        String mensagem = erro.getMessage() == null ? "" : erro.getMessage().toLowerCase(Locale.ROOT);
        for (String trecho : trechos) {
            assertTrue(mensagem.contains(trecho.toLowerCase(Locale.ROOT)), () -> "Mensagem: " + mensagem);
        }
    }

    private boolean tabelaExiste(String tabela) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     SELECT 1 FROM information_schema.tables
                     WHERE table_schema = ? AND table_name = ?
                     """)) {
            ps.setString(1, schema);
            ps.setString(2, tabela);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean indiceExiste(String nome) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     SELECT 1 FROM pg_indexes WHERE schemaname = ? AND indexname = ?
                     """)) {
            ps.setString(1, schema);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean constraintExiste(String nome, String tipo) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     SELECT 1 FROM information_schema.table_constraints
                     WHERE constraint_schema = ? AND constraint_name = ? AND constraint_type = ?
                     """)) {
            ps.setString(1, schema);
            ps.setString(2, nome);
            ps.setString(3, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String fkTarget(String nome) throws SQLException {
        try (Connection conexao = connection();
             PreparedStatement ps = conexao.prepareStatement("""
                     SELECT ccu.table_name
                     FROM information_schema.table_constraints tc
                     JOIN information_schema.constraint_column_usage ccu
                       ON ccu.constraint_schema = tc.constraint_schema AND ccu.constraint_name = tc.constraint_name
                     WHERE tc.constraint_schema = ? AND tc.constraint_name = ? AND tc.constraint_type = 'FOREIGN KEY'
                     """)) {
            ps.setString(1, schema);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private List<String> versoesAplicadas() throws SQLException {
        try (Connection conexao = connection();
             Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank")) {
            List<String> versoes = new ArrayList<>();
            while (rs.next()) {
                versoes.add(rs.getString(1));
            }
            return versoes;
        }
    }
}
