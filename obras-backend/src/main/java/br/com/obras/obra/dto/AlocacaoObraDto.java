package br.com.obras.obra.dto;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.usuario.domain.Cargo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record AlocacaoObraDto(
        Long id,
        Long usuarioId,
        String nome,
        String nomeUsuario,
        LocalDate dataInicio,
        LocalDate dataFim,
        List<CargoDto> cargos
) {
    public static AlocacaoObraDto de(AlocacaoObra alocacao) {
        String nome = alocacao.getUsuario().getNome();
        List<CargoDto> cargos = alocacao.getCargos().stream()
                .sorted(Comparator.comparing(Cargo::getNome))
                .map(CargoDto::de)
                .toList();
        return new AlocacaoObraDto(alocacao.getId(), alocacao.getUsuario().getId(),
                nome, nome, alocacao.getDataInicio(), alocacao.getDataFim(), cargos);
    }
}
