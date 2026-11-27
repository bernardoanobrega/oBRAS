package br.com.obras.obra.dto;

import br.com.obras.usuario.domain.Cargo;

public record CargoDto(Long id, String nome) {
    public static CargoDto de(Cargo cargo) {
        return new CargoDto(cargo.getId(), cargo.getNome());
    }
}
