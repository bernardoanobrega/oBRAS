package br.com.obras.obra.dto;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.usuario.domain.Cargo;

import java.time.LocalDate;
import java.util.List;

public record EquipeMembroDto(
    Long usuarioId,
    String nome,
    List<String> cargos,
    LocalDate dataInicio
) {
    public static EquipeMembroDto de(AlocacaoObra a) {
        return new EquipeMembroDto(
            a.getUsuario().getId(),
            a.getUsuario().getNome(),
            a.getCargos().stream()
                .map(Cargo::getNome)
                .sorted()
                .toList(),
            a.getDataInicio()
        );
    }
}