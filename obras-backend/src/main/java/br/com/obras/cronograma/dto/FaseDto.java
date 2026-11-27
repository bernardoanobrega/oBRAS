package br.com.obras.cronograma.dto;

import br.com.obras.cronograma.domain.Fase;
import br.com.obras.cronograma.domain.enums.SituacaoPrazo;
import br.com.obras.cronograma.domain.enums.StatusFase;

import java.time.LocalDate;

public record FaseDto(Long id, String codigo, String nome, String descricao, int ordem, long duracaoDias, 
                    LocalDate inicioPrevisto, LocalDate fimPrevisto, LocalDate inicioReal, LocalDate fimReal, StatusFase status, SituacaoPrazo situacao, double percentualConcluido, int desvioDias, int quantidadeAvancos) {
    public static FaseDto de(Fase fase, LocalDate dataReferencia) {
        return new FaseDto(fase.getId(), String.format("F-%02d", fase.getOrdem()), fase.getNome(),
                fase.getDescricao(), fase.getOrdem(), fase.calcularDuracaoDias(), fase.getInicioPrevisto(),
                fase.getFimPrevisto(), fase.getInicioReal(), fase.getFimReal(), fase.getStatus(),
                fase.classificarSituacao(dataReferencia), fase.calcularPercentualConcluido(),
                fase.calcularDesvioDias(dataReferencia), fase.getAvancos().size());
    }
}
