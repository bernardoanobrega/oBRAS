package br.com.obras.obra.dto;

import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PainelObraDto(
    ObraDto obra,
    ClimaDto clima,
    KpisDto kpis,
    List<EquipeMembroDto> equipe,
    MaoDeObraDto maoDeObra,
    List<OcorrenciaDto> ocorrencias,
    List<FotoDto> fotos
) {
    public record ClimaDto(LocalDate data, String condicao) {
        public static ClimaDto ausente() {
            return new ClimaDto(null, null);
        }
    }

    public record KpisDto(
        BigDecimal avancoRealizado,
        BigDecimal avancoPrevisto,
        BigDecimal spi,
        BigDecimal orcamentoTotal,
        BigDecimal executado,
        BigDecimal limiteAlerta,
        String fonte
    ) {
        public static final String FONTE_SIMULADO = "SIMULADO";

        public static KpisDto simulados() {
            return new KpisDto(null, null, null, null, null, null, FONTE_SIMULADO);
        }
    }

    public record MaoDeObraDto(int alocados, int presentesHoje, Integer vsOntem, String fonte) {
        public static final String FONTE_DIARIO = "DIARIO";

        public static MaoDeObraDto de(int alocados, int presentesHoje, Integer vsOntem) {
            return new MaoDeObraDto(alocados, presentesHoje, vsOntem, FONTE_DIARIO);
        }
    }
}