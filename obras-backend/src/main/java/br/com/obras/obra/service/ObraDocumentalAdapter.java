package br.com.obras.obra.service;

import br.com.obras.shared.domain.ObraDocumentalPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ObraDocumentalAdapter implements ObraDocumentalPort {
    private final JdbcClient jdbc;
    public ObraDocumentalAdapter(JdbcClient jdbc) { this.jdbc = jdbc; }
    public List<ObraResumo> listar() {
        return jdbc.sql("SELECT id, nome FROM obra ORDER BY nome").query(ObraResumo.class).list();
    }
    public boolean existe(Long id) {
        return jdbc.sql("SELECT EXISTS(SELECT 1 FROM obra WHERE id = :id)").param("id", id).query(Boolean.class).single();
    }
}
