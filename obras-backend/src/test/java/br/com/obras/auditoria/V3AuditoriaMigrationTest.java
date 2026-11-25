package br.com.obras.auditoria;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class V3AuditoriaMigrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private String schema;

    @Test
    void migrationV3CriaTabelasDeAuditoria() throws SQLException {
        setupLatest();

        ColumnInfo auditavelId = coluna("auditavel", "id");
        ColumnInfo auditavelExcluido = coluna("auditavel", "excluido");
        ColumnInfo registroId = coluna("registro_auditoria", "id");
        ColumnInfo registroAuditavel = coluna("registro_auditoria", "auditavel_id");
        ColumnInfo registroUsuario = coluna("registro_auditoria", "usuario_id");
        ColumnInfo registroAcao = coluna("registro_auditoria", "acao");
        ColumnInfo registroDataHora = coluna("registro_auditoria", "data_hora");
        ColumnInfo registroDetalhe = coluna("registro_auditoria", "detalhe");

        assertAll(
                () -> assertEquals("bigint", auditavelId.dataType()),
                () -> assertEquals("YES", auditavelId.identity()),
                () -> assertEquals("ALWAYS", auditavelId.identityGeneration()),
                () -> assertEquals("boolean", auditavelExcluido.dataType()),
                () -> assertEquals("NO", auditavelExcluido.nullable()),
                () -> assertTrue(normalizarDefault(auditavelExcluido.defaultValue()).contains("false")),
                () -> assertEquals("bigint", registroId.dataType()),
                () -> assertEquals("YES", registroId.identity()),
                () -> assertEquals("ALWAYS", registroId.identityGeneration()),
                () -> assertEquals("bigint", registroAuditavel.dataType()),
                () -> assertEquals("NO", registroAuditavel.nullable()),
                () -> assertEquals("bigint", registroUsuario.dataType()),
                () -> assertEquals("NO", registroUsuario.nullable()),
                () -> assertEquals("character varying", registroAcao.dataType()),
                () -> assertEquals("NO", registroAcao.nullable()),
                () -> assertEquals("timestamp without time zone", registroDataHora.dataType()),
                () -> assertEquals("NO", registroDataHora.nullable()),
                () -> assertTrue(normalizarDefault(registroDataHora.defaultValue()).contains("now")),
                () -> assertEquals("jsonb", registroDetalhe.dataType()),
                () -> assertEquals("YES", registroDetalhe.nullable())
        );
    }

    @Test
    void indiceAuditavelDataHoraExiste() throws SQLException {
        setupLatest();

        assertEquals(List.of("{auditavel_id,data_hora}"), colunasIndice("ix_reg_auditoria_auditavel"));
    }

    @Test
    void constraintsDeAuditoriaExistem() throws SQLException {
        setupLatest();

        assertAll(
                () -> assertTrue(constraintExiste("ck_reg_auditoria_acao", "CHECK")),
                () -> assertTrue(constraintExiste("fk_reg_auditoria_auditavel", "FOREIGN KEY")),
                () -> assertTrue(constraintExiste("fk_reg_auditoria_usuario", "FOREIGN KEY")),
                () -> assertEquals("auditavel", fkTarget("fk_reg_auditoria_auditavel")),
                () -> assertEquals("usuario", fkTarget("fk_reg_auditoria_usuario"))
        );
    }

    @Test
    void auditavelIniciaNaoExcluido() throws SQLException {
        setupLatest();

        try (Connection conexao = connection()) {
            long id = insertAuditavel(conexao);
            try (PreparedStatement ps = conexao.prepareStatement("SELECT excluido FROM auditavel WHERE id = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertFalse(rs.getBoolean(1));
                }
            }
        }
    }

    @Test
    void registroAuditoriaAceitaDetalheJsonb() throws SQLException {
        setupLatest();
        String json = """
                {"chave":"valor","n":1}
                """.trim();

        try (Connection conexao = connection()) {
            long auditavelId = insertAuditavel(conexao);
            long usuarioId = insertUsuario(conexao, "json1");
            long registroId = insertRegistro(conexao, auditavelId, usuarioId, "CRIACAO", json, true);

            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT detalhe = ?::jsonb FROM registro_auditoria WHERE id = ?")) {
                ps.setString(1, json);
                ps.setLong(2, registroId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertTrue(rs.getBoolean(1));
                }
            }
        }
    }

    @Test
    void registroAuditoriaPreencheDataHoraPorDefaultQuandoOmitida() throws SQLException {
        setupLatest();

        try (Connection conexao = connection()) {
            long auditavelId = insertAuditavel(conexao);
            long usuarioId = insertUsuario(conexao, "default1");
            long registroId = insertRegistro(conexao, auditavelId, usuarioId, "EDICAO", null, false);

            try (PreparedStatement ps = conexao.prepareStatement("SELECT data_hora FROM registro_auditoria WHERE id = ?")) {
                ps.setLong(1, registroId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertNotNull(rs.getTimestamp(1));
                }
            }
        }
    }

    @Test
    void registroAuditoriaRejeitaAcaoInvalida() throws SQLException {
        setupDependencies();

        assertViolacao(
                "INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao) VALUES ("
                        + auditavelId() + ", " + usuarioId() + ", 'FOO')",
                "ck_reg_auditoria_acao");
    }

    @Test
    void registroAuditoriaRejeitaAuditavelIdInvalido() throws SQLException {
        setupDependencies();

        assertViolacao(
                "INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao) VALUES ("
                        + Long.MAX_VALUE + ", " + usuarioId() + ", 'CRIACAO')",
                "fk_reg_auditoria_auditavel");
    }

    @Test
    void registroAuditoriaRejeitaUsuarioIdInvalido() throws SQLException {
        setupDependencies();

        assertViolacao(
                "INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao) VALUES ("
                        + auditavelId() + ", " + Long.MAX_VALUE + ", 'CRIACAO')",
                "fk_reg_auditoria_usuario");
    }

    @Test
    void registroAuditoriaRejeitaUsuarioIdNulo() throws SQLException {
        setupDependencies();

        assertViolacao(
                "INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao) VALUES ("
                        + auditavelId() + ", NULL, 'CRIACAO')",
                "usuario_id", "not-null");
    }

    @Test
    void registroAuditoriaRejeitaAuditavelIdNulo() throws SQLException {
        setupDependencies();

        assertViolacao(
                "INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao) VALUES ("
                        + "NULL, " + usuarioId() + ", 'CRIACAO')",
                "auditavel_id", "not-null");
    }

    @Test
    void migrationV3AplicaSobreV2ComDados() throws SQLException {
        schema = freshSchema();
        migrate("2");

        long usuarioId;
        long obraId;
        try (Connection conexao = connection()) {
            usuarioId = insertUsuario(conexao, "resp1");
            obraId = insertObra(conexao, usuarioId);
        }
        assertEquals(List.of("1", "2"), versoesAplicadas());

        migrate("4");
        assertEquals(List.of("1", "2", "3", "3.1", "4"), versoesAplicadas());

        try (Connection conexao = connection()) {
            try (PreparedStatement ps = conexao.prepareStatement("SELECT login FROM usuario WHERE id = ?")) {
                ps.setLong(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("resp1", rs.getString(1));
                }
            }
            try (PreparedStatement ps = conexao.prepareStatement("SELECT nome FROM obra WHERE id = ?")) {
                ps.setLong(1, obraId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("Obra V2", rs.getString(1));
                }
            }
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name IN ('auditavel','registro_auditoria')")) {
                ps.setString(1, schema);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }
            }
        }
    }

    private void setupLatest() {
        schema = freshSchema();
        migrate(null);
    }

    private void setupDependencies() throws SQLException {
        setupLatest();
        try (Connection conexao = connection()) {
            dependencias = new long[]{insertAuditavel(conexao), insertUsuario(conexao, "aud1")};
        }
    }

    private long[] dependencias;

    private long auditavelId() {
        return dependencias[0];
    }

    private long usuarioId() {
        return dependencias[1];
    }

    private void migrate(String target) {
        String url = jdbcUrl(schema);
        var config = Flyway.configure()
                .dataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .schemas(schema)
                .defaultSchema(schema);
        if (target != null) {
            config.target(MigrationVersion.fromVersion(target));
        }
        config.load().migrate();
    }

    private String freshSchema() {
        String nome = "aud_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection conexao = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = conexao.createStatement()) {
            st.executeUpdate("CREATE SCHEMA " + nome);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return nome;
    }

    private Connection connection() throws SQLException {
        return connection(schema);
    }

    private Connection connection(String nomeSchema) throws SQLException {
        return DriverManager.getConnection(jdbcUrl(nomeSchema), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private String jdbcUrl(String nomeSchema) {
        String base = POSTGRES.getJdbcUrl();
        return base + (base.contains("?") ? "&" : "?") + "currentSchema=" + nomeSchema;
    }

    private long insertAuditavel(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private long insertUsuario(Connection conexao, String login) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "INSERT INTO usuario (nome, situacao, login, senha_hash) VALUES (?, 'ATIVO', ?, 'hash') RETURNING id")) {
            ps.setString(1, "Resp " + login);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertObra(Connection conexao, long responsavelId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto, status, responsavel_id)
                VALUES ('Obra V2', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31', 'PLANEJADA', ?)
                RETURNING id
                """)) {
            ps.setLong(1, responsavelId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertRegistro(Connection conexao, Long auditavel, Long usuario, String acao,
                                String detalhe, boolean informarDataHora) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO registro_auditoria (auditavel_id, usuario_id, acao");
        if (informarDataHora) {
            sql.append(", data_hora");
        }
        if (detalhe != null) {
            sql.append(", detalhe");
        }
        sql.append(") VALUES (?, ?, ?");
        if (informarDataHora) {
            sql.append(", TIMESTAMP '2026-11-18 10:00:00'");
        }
        if (detalhe != null) {
            sql.append(", ?::jsonb");
        }
        sql.append(") RETURNING id");

        try (PreparedStatement ps = conexao.prepareStatement(sql.toString())) {
            ps.setObject(1, auditavel);
            ps.setObject(2, usuario);
            ps.setString(3, acao);
            if (detalhe != null) {
                ps.setString(4, detalhe);
            }
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private void assertViolacao(String sql, String... trechos) throws SQLException {
        SQLException erro = assertThrows(SQLException.class, () -> {
            try (Connection conexao = connection(); Statement st = conexao.createStatement()) {
                st.executeUpdate(sql);
            }
        });
        String mensagem = erro.getMessage() == null ? "" : erro.getMessage().toLowerCase(Locale.ROOT);
        for (String trecho : trechos) {
            assertTrue(mensagem.contains(trecho.toLowerCase(Locale.ROOT)), () -> "Mensagem: " + mensagem);
        }
    }

    private ColumnInfo coluna(String tabela, String coluna) throws SQLException {
        String sql = """
                SELECT data_type, is_nullable, column_default, is_identity, identity_generation
                FROM information_schema.columns
                WHERE table_schema = ? AND table_name = ? AND column_name = ?
                """;
        try (Connection conexao = connection(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, tabela);
            ps.setString(3, coluna);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Coluna ausente: " + tabela + "." + coluna);
                return new ColumnInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
            }
        }
    }

    private boolean constraintExiste(String nome, String tipo) throws SQLException {
        String sql = """
                SELECT 1
                FROM information_schema.table_constraints
                WHERE constraint_schema = ? AND constraint_name = ? AND constraint_type = ?
                """;
        try (Connection conexao = connection(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, nome);
            ps.setString(3, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String fkTarget(String nome) throws SQLException {
        String sql = """
                SELECT ccu.table_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.constraint_column_usage ccu
                  ON ccu.constraint_schema = tc.constraint_schema AND ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_schema = ? AND tc.constraint_name = ? AND tc.constraint_type = 'FOREIGN KEY'
                """;
        try (Connection conexao = connection(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private List<String> colunasIndice(String nomeIndice) throws SQLException {
        String sql = """
                SELECT array_agg(a.attname ORDER BY k.ordinality)::text
                FROM pg_index x
                JOIN pg_class i ON i.oid = x.indexrelid
                JOIN pg_class t ON t.oid = x.indrelid
                JOIN pg_namespace n ON n.oid = t.relnamespace
                JOIN LATERAL unnest(x.indkey) WITH ORDINALITY AS k(attnum, ordinality) ON true
                JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
                WHERE n.nspname = ? AND i.relname = ? AND t.relname = 'registro_auditoria'
                GROUP BY i.relname
                """;
        try (Connection conexao = connection(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, nomeIndice);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? List.of(rs.getString(1)) : List.of();
            }
        }
    }

    private List<String> versoesAplicadas() throws SQLException {
        String sql = "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank";
        try (Connection conexao = connection();
             Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<String> versoes = new ArrayList<>();
            while (rs.next()) {
                versoes.add(rs.getString(1));
            }
            return versoes;
        }
    }

    private String normalizarDefault(String valor) {
        return valor == null ? "" : valor.toLowerCase(Locale.ROOT);
    }

    private record ColumnInfo(String dataType, String nullable, String defaultValue,
                              String identity, String identityGeneration) {
    }
}
