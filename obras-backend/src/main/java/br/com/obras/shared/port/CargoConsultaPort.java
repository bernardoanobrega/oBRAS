package br.com.obras.shared.port;

import br.com.obras.usuario.domain.Cargo;

import java.util.List;
import java.util.Set;

public interface CargoConsultaPort {
    List<Cargo> buscarPorIds(Set<Long> ids);

    List<Cargo> listar();
}
