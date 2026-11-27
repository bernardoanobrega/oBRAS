package br.com.obras.equipamento.dto;

import jakarta.validation.constraints.Size;

public record DevolverEquipamentoDto(
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres.")
        String observacao) {
}