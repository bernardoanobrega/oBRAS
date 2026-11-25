package br.com.obras.estoque.dto;

import br.com.obras.estoque.domain.Material;

public record MaterialDto(Long id, String nome, String descricao, String unidadeMedida) {
    public static MaterialDto de(Material m) {
        return new MaterialDto(m.getId(), m.getNome(), m.getDescricao(), m.getUnidadeMedida());
    }
}
