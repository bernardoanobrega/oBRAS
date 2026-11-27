package br.com.obras.cronograma.dto;

import br.com.obras.cronograma.domain.AvancoFisico;

import java.time.LocalDate;

public record AvancoFisicoDto(Long id, Long faseId, LocalDate dataRegistro, double percentualAcumulado, String observacao, Long autorId) {
    public static AvancoFisicoDto de(AvancoFisico avanco) {
        return new AvancoFisicoDto(avanco.getId(), avanco.getFase().getId(), avanco.getDataRegistro(), avanco.getPercentualAcumulado(), avanco.getObservacao(), avanco.getAutorId());
    }
}
