package br.com.obras.equipamento.dto;

import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;

public record EquipamentoDto(
        Long id,
        String codigoPatrimonio,
        String descricao,
        TipoEquipamento tipo,
        StatusEquipamento status) {

    public static EquipamentoDto de(Equipamento equipamento) {
        return new EquipamentoDto(
                equipamento.getId(),
                equipamento.getCodigoPatrimonio(),
                equipamento.getDescricao(),
                equipamento.getTipo(),
                equipamento.getStatus());
    }
}