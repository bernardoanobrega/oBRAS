package br.com.obras.shared.port;

import java.util.List;
import java.util.Optional;

/** Consultas mínimas a obra/canteiro. Implementada pelo módulo obra. */
public interface ObraConsultaPort {
    boolean obraExiste(Long obraId);

    Optional<Long> obraIdDoCanteiro(Long canteiroId);

    List<Long> canteiroIdsDaObra(Long obraId);
}
