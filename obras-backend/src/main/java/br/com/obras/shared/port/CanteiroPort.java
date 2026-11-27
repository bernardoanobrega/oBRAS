package br.com.obras.shared.port;

import java.util.List;
import java.util.Optional;

/** Acesso de outros módulos a canteiros. O adapter fica no módulo obra. */
public interface CanteiroPort {

    Optional<CanteiroResumo> buscarResumo(Long canteiroId);

    List<Long> listarIdsPorObra(Long obraId);
}