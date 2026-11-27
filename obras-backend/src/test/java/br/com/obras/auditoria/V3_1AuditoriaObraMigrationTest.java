package br.com.obras.auditoria;

import org.flywaydb.core.Flyway;
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class V3_1AuditoriaObraMigrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private String schema;

    @Test
    void versaoV31Aplicada() throws SQLException {
        setup(null);
        assertTrue(versoesAplicadas().contains("3.1"));
    }

    @Test
    void colunaAuditavelIdExisteComConstraints() throws SQLException {
        setup(null);
        try (Connection conexao = connection()) {
            assertAll(
                () -> assertEquals("NO", nullable(conexao, "obra", "auditavel_id")),
                () -> assertEquals("YES", nullable(conexao, "obra", "responsavel_id")),
                () -> assertTrue(constraintExiste(conexao, "uk_obra_auditavel", "UNIQUE")),
                () -> assertTrue(constraintExiste(conexao, "fk_obra_auditavel", "FOREIGN KEY")),
                () -> assertEquals("auditavel", fkTarget(conexao, "fk_obra_auditavel"))
            );
        }
    }

    @Test
    void backfillCriaUmAuditavelPorObra() throws SQLException {
        setup("2");
        long usuarioId;
        long obraA;
        long obraB;
        try (Connection conexao = connection()) {
            usuarioId = insertUsuario(conexao);
            obraA = insertObraV2(conexao, usuarioId, "Obra A");
            obraB = insertObraV2(conexao, usuarioId, "Obra B");
        }
        migrate(null);

        try (Connection conexao = connection()) {
            long auditavelA = auditavelDaObra(conexao, obraA);
            long auditavelB = auditavelDaObra(conexao, obraB);
            assertAll(
                () -> assertNotEquals(auditavelA, auditavelB),
                () -> assertEquals(2, contar(conexao, "auditavel")),
                () -> assertFalse(excluido(conexao, auditavelA)),
                () -> assertFalse(excluido(conexao, auditavelB))
            );
        }
    }

    @Test
    void obraAceitaResponsavelNuloAposMigration() throws SQLException {
        setup(null);
        long obraId;
        try (Connection conexao = connection()) {
            obraId = insertObraAtual(conexao, null);
        }
        try (Connection conexao = connection()) {
            assertTrue(responsavelNulo(conexao, obraId));
        }
    }

    @Test
    void obraNaoAceitaAuditavelDuplicado() throws SQLException {
        setup(null);
        try (Connection conexao = connection()) {
            long auditavelId = insertAuditavel(conexao);
            long obraA = insertObraComAuditavel(conexao, "Obra A", auditavelId);

            SQLException violacao = assertThrows(SQLException.class,
                    () -> insertObraComAuditavel(conexao, "Obra B", auditavelId));

            String mensagem = violacao.getMessage() == null ? "" : violacao.getMessage();
            assertAll(
                    () -> assertTrue(obraA > 0),
                    () -> assertTrue(
                            "23505".equals(violacao.getSQLState()) || mensagem.contains("uk_obra_auditavel"),
                            "esperava violação de UNIQUE em uk_obra_auditavel, veio: "
                                    + violacao.getSQLState() + " / " + mensagem),
                    () -> assertEquals(1L, contarObrasPorAuditavel(conexao, auditavelId))
            );
        }
    }

    @Test
    void obraExistenteMantemResponsavel() throws SQLException {
        setup("2");
        long usuarioId;
        long obraId;
        try (Connection conexao = connection()) {
            usuarioId = insertUsuario(conexao);
            obraId = insertObraV2(conexao, usuarioId, "Obra existente");
        }

        migrate(null);

        try (Connection conexao = connection()) {
            long responsavel = responsavelDaObra(conexao, obraId);
            long auditavelId = auditavelDaObra(conexao, obraId);
            assertAll(
                    () -> assertEquals(usuarioId, responsavel),
                    () -> assertFalse(responsavelNulo(conexao, obraId)),
                    () -> assertTrue(auditavelId > 0),
                    () -> assertFalse(excluido(conexao, auditavelId))
            );
        }
    }

    private void setup(String target) {
        schema = freshSchema();
        migrate(target);
    }

    private void migrate(String target) {
        var config = Flyway.configure()
            .dataSource(jdbcUrl(schema), POSTGRES.getUsername(), POSTGRES.getPassword())
            .locations("classpath:db/migration")
            .schemas(schema)
            .defaultSchema(schema);
        if (target != null) {
            config.target(org.flywaydb.core.api.MigrationVersion.fromVersion(target));
        }
        config.load().migrate();
    }

    private String freshSchema() {
        String nome = "v31_" + UUID.randomUUID().toString().replace("-", "");
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
        return DriverManager.getConnection(jdbcUrl(schema), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private String jdbcUrl(String nomeSchema) {
        String base = POSTGRES.getJdbcUrl();
        return base + (base.contains("?") ? "&" : "?") + "currentSchema=" + nomeSchema;
    }

    private long insertUsuario(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Responsavel', 'ATIVO', ?, 'hash') RETURNING id
                """)) {
            ps.setString(1, "resp-" + UUID.randomUUID());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertObraV2(Connection conexao, long responsavelId, String nome) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto, status, responsavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31', 'PLANEJADA', ?)
                RETURNING id
                """)) {
            ps.setString(1, nome);
            ps.setLong(2, responsavelId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertAuditavel(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private long insertObraAtual(Connection conexao, Long responsavelId) throws SQLException {
        long auditavelId = insertAuditavel(conexao);
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto, status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua Y', DATE '2026-01-01', DATE '2026-12-31', 'PLANEJADA', ?, ?)
                RETURNING id
                """)) {
            ps.setString(1, "Obra atual");
            ps.setObject(2, responsavelId, Types.BIGINT);
            ps.setLong(3, auditavelId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long insertObraComAuditavel(Connection conexao, String nome, long auditavelId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto, status,
                                  responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31', 'PLANEJADA', NULL, ?)
                RETURNING id
                """)) {
            ps.setString(1, nome);
            ps.setLong(2, auditavelId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long contarObrasPorAuditavel(Connection conexao, long auditavelId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT count(*) FROM obra WHERE auditavel_id = ?")) {
            ps.setLong(1, auditavelId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long responsavelDaObra(Connection conexao, long obraId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT responsavel_id FROM obra WHERE id = ?")) {
            ps.setLong(1, obraId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private long auditavelDaObra(Connection conexao, long obraId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT auditavel_id FROM obra WHERE id = ?")) {
            ps.setLong(1, obraId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private boolean excluido(Connection conexao, long auditavelId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT excluido FROM auditavel WHERE id = ?")) {
            ps.setLong(1, auditavelId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBoolean(1);
            }
        }
    }

    private boolean responsavelNulo(Connection conexao, long obraId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(
                "SELECT responsavel_id IS NULL FROM obra WHERE id = ?")) {
            ps.setLong(1, obraId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBoolean(1);
            }
        }
    }

    private long contar(Connection conexao, String tabela) throws SQLException {
        try (Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM " + tabela)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private String nullable(Connection conexao, String tabela, String coluna) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
                SELECT is_nullable FROM information_schema.columns
                WHERE table_schema = ? AND table_name = ? AND column_name = ?
                """)) {
            ps.setString(1, schema);
            ps.setString(2, tabela);
            ps.setString(3, coluna);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private boolean constraintExiste(Connection conexao, String nome, String tipo) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
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

    private String fkTarget(Connection conexao, String nome) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("""
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
