package br.com.obras.cronograma.dto;

import br.com.obras.cronograma.domain.Cronograma;
import br.com.obras.cronograma.domain.Fase;
import br.com.obras.cronograma.domain.enums.StatusFase;

import java.time.LocalDate;
import java.util.List;

public record IndicadoresCronogramaDto(double percentualGeral, double percentualPlanejado, double variacaoPlano, int totalAtividades, int concluidas, int noPrazo, int atrasadas, int desvioPrazoDias) {
    public static IndicadoresCronogramaDto de(Cronograma cronograma, LocalDate dataReferencia) {
        List<Fase> ativas = cronograma.getFases().stream()
                .filter(fase -> fase.getStatus() != StatusFase.CANCELADA)
                .toList();
        int concluidas = (int) ativas.stream().filter(fase -> fase.getStatus() == StatusFase.CONCLUIDA).count();
        int atrasadas = cronograma.identificarFasesAtrasadas(dataReferencia).size();
        double geral = cronograma.calcularPercentualGeral();
        double planejado = cronograma.calcularPercentualPlanejado(dataReferencia);
        return new IndicadoresCronogramaDto(arredondar(geral), arredondar(planejado), arredondar(geral - planejado), ativas.size(), concluidas, ativas.size() - atrasadas, atrasadas, cronograma.calcularDesvioPrazoDias(dataReferencia));
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
