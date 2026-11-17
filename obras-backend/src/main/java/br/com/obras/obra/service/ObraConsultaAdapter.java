package br.com.obras.obra.service;

import br.com.obras.shared.port.ObraConsultaPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Troque por ObraRepository/CanteiroRepository quando as entidades do módulo obra existirem. */
@Component
public class ObraConsultaAdapter implements ObraConsultaPort {
    private final JdbcClient jdbc;

    public ObraConsultaAdapter(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public boolean obraExiste(Long obraId) {
        Long total = jdbc.sql("SELECT count(*) FROM obra WHERE id = :id")
                .param("id", obraId).query(Long.class).single();
        return total > 0;
    }

    @Override
    public Optional<Long> obraIdDoCanteiro(Long canteiroId) {
        return jdbc.sql("SELECT obra_id FROM canteiro WHERE id = :id")
                .param("id", canteiroId).query(Long.class).optional();
    }

    @Override
    public List<Long> canteiroIdsDaObra(Long obraId) {
        return jdbc.sql("SELECT id FROM canteiro WHERE obra_id = :id ORDER BY id")
                .param("id", obraId).query(Long.class).list();
    }
}
