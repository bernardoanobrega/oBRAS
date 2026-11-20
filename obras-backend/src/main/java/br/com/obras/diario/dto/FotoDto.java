package br.com.obras.diario.dto;

import br.com.obras.diario.domain.RegistroFotografico;

import java.time.LocalDateTime;

public record FotoDto(
    Long id,
    Long ocorrenciaId,
    String nomeArquivo,
    String legenda,
    LocalDateTime dataEnvio
) {
    public static FotoDto de(RegistroFotografico f) {
        String localizador = f.getArquivo();
        String nome = localizador.substring(localizador.lastIndexOf('/') + 1);
        return new FotoDto(f.getId(), f.getOcorrencia().getId(), nome, f.getLegenda(), f.getDataEnvio());
    }
}
