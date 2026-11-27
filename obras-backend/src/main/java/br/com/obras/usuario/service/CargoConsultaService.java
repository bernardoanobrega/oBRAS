package br.com.obras.usuario.service;

import br.com.obras.shared.port.CargoConsultaPort;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.repository.CargoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class CargoConsultaService implements CargoConsultaPort {
    private final CargoRepository cargos;

    public CargoConsultaService(CargoRepository cargos) {
        this.cargos = cargos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cargo> buscarPorIds(Set<Long> ids) {
        return cargos.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cargo> listar() {
        return cargos.findAll();
    }
}
