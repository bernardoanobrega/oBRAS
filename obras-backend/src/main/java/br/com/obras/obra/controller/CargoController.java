package br.com.obras.obra.controller;

import br.com.obras.obra.dto.CargoDto;
import br.com.obras.shared.port.CargoConsultaPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
public class CargoController {
    private final CargoConsultaPort cargos;

    public CargoController(CargoConsultaPort cargos) {
        this.cargos = cargos;
    }

    @GetMapping
    @PreAuthorize("@perm.podeGlobal('DETALHES_OBRA', 'EDITAR') or " +
                  "@perm.podeEmAlgumaObra('DETALHES_OBRA', 'EDITAR')")
    public List<CargoDto> listar() {
        return cargos.listar().stream().map(CargoDto::de).toList();
    }

    @PostMapping
    @PreAuthorize("@perm.podeGlobal('DETALHES_OBRA', 'EDITAR') or " +
                  "@perm.podeEmAlgumaObra('DETALHES_OBRA', 'EDITAR')")
    public void criar() {
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.podeGlobal('DETALHES_OBRA', 'EDITAR') or " +
                  "@perm.podeEmAlgumaObra('DETALHES_OBRA', 'EDITAR')")
    public void atualizar(@PathVariable Long id) {
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.podeGlobal('DETALHES_OBRA', 'EDITAR') or " +
                  "@perm.podeEmAlgumaObra('DETALHES_OBRA', 'EDITAR')")
    public void excluir(@PathVariable Long id) {
    }
}
