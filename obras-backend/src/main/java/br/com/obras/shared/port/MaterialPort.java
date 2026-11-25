package br.com.obras.shared.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Catálogo de materiais visto por compras. Implementada pelo módulo estoque. */
public interface MaterialPort {
    Optional<MaterialResumo> buscar(Long materialId);

    Map<Long, MaterialResumo> buscarVarios(Collection<Long> materialIds);

    record MaterialResumo(Long id, String nome, String unidadeMedida) {}
}
