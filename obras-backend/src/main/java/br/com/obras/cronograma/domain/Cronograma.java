package br.com.obras.cronograma.domain;

import br.com.obras.cronograma.domain.enums.StatusCronograma;
import br.com.obras.cronograma.domain.enums.StatusFase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity 
public class Cronograma {
    // ===== ATTRIBUTES =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataUltimaAtualizacao;
    
    @Enumerated(EnumType.STRING)
    private StatusCronograma status;

    @OneToMany(mappedBy = "cronograma", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private final List<Fase> fases = new ArrayList<>();

    @OneToOne
    private Obra obra;

    // ===== CONSTRUCTORS =====
    public Cronograma(Obra obra) {
        this.obra = obra;
        this.dataUltimaAtualizacao = LocalDate.now();
        this.status = StatusCronograma.VAZIO;
    }

    // ===== METHODS =====

    /**
     * Método para adicionar uma fase nova ao cronograma
     * @param nome
     * @param descricao
     * @param ordem
     * @param inicioPrevisto
     * @param fimPrevisto
     * @return Se receber dados válidos, retorna a fase já adicionada
     */
    public Fase adicionarFase(String nome, String descricao, int ordem, LocalDate inicioPrevisto, LocalDate fimPrevisto) {
        checarEditabilidade();

        if(nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da fase é obrigatório.");
        }

        if(ordem < 1) {
            throw new IllegalArgumentException("A ordem deve ser maior ou igual a 1.");
        }

        boolean ordemJaUsada = fases.stream().anyMatch(f -> f.getOrdem() == ordem);
        if(ordemJaUsada) {
            throw new IllegalArgumentException("Já existe uma fase de ordem " + ordem);
        }

        Fase fase = new Fase(this, nome, descricao, ordem, inicioPrevisto, fimPrevisto);
        if(!fase.validarPeriodo()) {
            throw new IllegalArgumentException("Período inválido: o início previsto deve ser anterior ou igual ao fim previsto");
        }

        fases.add(fase);
        fases.sort(Comparator.comparingInt(Fase::getOrdem));
        registrarAtualizacao();

        return fase;
    }    

    /**
     * Método para remover uma fase do cronograma
     * @param idFase
     */
    public void removerFase(Long idFase) {
        checarEditabilidade();

        Fase fase = buscarFase(idFase);
        if(fase == null) {
            throw new IllegalArgumentException("Fase com id " + idFase + " não encontrada.");
        }
        if(fase.getStatus() != StatusFase.NAO_INICIADA) {
            throw new IllegalStateException("Não é permitido remover fases já iniciadas.");
        }

        fases.remove(fase);
        registrarAtualizacao();
    }

    /**
     * Método para buscar uma fase no cronograma a partir do Id
     * @param idFase
     * @return Retorna a fase com o id informado ou null se não existir
     */
    public Fase buscarFase(Long idFase) {
        if(idFase == null) {
            return null;
        }
        return fases.stream()
                .filter(f -> idFase.equals(f.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Método para validar a sequência das ordem das fases de um cronograma
     * @return Valor booleano que indica se a sequência é válida
     */
    public boolean validarSequenciaFases() {
        List<Fase> fasesOrdenadas = new ArrayList<>(fases);
        fasesOrdenadas.sort(Comparator.comparingInt(Fase::getOrdem));

        for(int i = 0; i < fasesOrdenadas.size(); i++) {
            Fase fase = fasesOrdenadas.get(i);
            if(fase.getOrdem() != i + 1 || !fase.validarPeriodo()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Método para obter o percentual já concluído do cronograma
     * @return Retorna um valor entre 0 e 1 representando o percentual concluído do cronograma
     */
    public double calcularPercentualGeral() {
        double somaPonderada = 0;
        long pesoTotal = 0;

        for(Fase fase : fases) {
            if(fase.getStatus() == StatusFase.CANCELADA) {
                continue;
            }
            long pesoFase = Math.max(1, ChronoUnit.DAYS.between(fase.getInicioPrevisto(), fase.getFimPrevisto()) + 1);
            somaPonderada += fase.calcularPercentualConcluido() * pesoFase;
            pesoTotal += pesoFase;
        }

        return pesoTotal == 0 ? 0.0 : somaPonderada / pesoTotal;
    }

    /**
     * Método para obter o maior desvio entre as fases fasesAtivas
     * positivo = atrasado, negativo = adiantado, 0 = em dia
     * @param dataReferencia
     * @return Retorna a quantidade de dias de desvio máxima encontrada
     */
    public int calcularDesvioPrazoDias(LocalDate dataReferencia) {
        return fases.stream()
                .filter(f -> f.getStatus() != StatusFase.CANCELADA)
                .mapToInt(f -> f.calcularDesvioDias(dataReferencia))
                .max()
                .orElse(0);
    }

    /**
     * Método para obter as fases com atraso no prazo
     * @param dataReferencia
     * @return Retorna uma lista com as fases atrasadas
     */
    public List<Fase> identificarFasesAtrasadas(LocalDate dataReferencia) {
        return fases.stream()
                .filter(f -> f.estaAtrasada(dataReferencia))
                .sorted(Comparator.comparingInt(Fase::getOrdem))
                .toList();
    }

    /**
     * Método para cancelar um cronograma e suas fases
     */
    public void cancelarCronograma() {
        if(status == StatusCronograma.CONCLUIDO) {
            throw new IllegalStateException("Um cronograma concluído não pode ser cancelado.");
        }
        if(status == StatusCronograma.CANCELADO) {
            return;
        }

        for(Fase fase : fases) {
            if(fase.getStatus() != StatusFase.CONCLUIDA && fase.getStatus() != StatusFase.CANCELADA) {
                fase.cancelarFase();
            }
        }

        status = StatusCronograma.CANCELADO;
        registrarAtualizacao();
    }

    // ===== AUXILIARY METHODS =====

    /**
     * Método para recalcular o status a partir das fases
     */
    public void atualizarStatus() {
        if (status == StatusCronograma.CANCELADO) {
            return;
        }
        
        List<Fase> fasesAtivas = fases.stream()
                                .filter(f -> f.getStatus() != StatusFase.CANCELADA)
                                .toList();
        
        if (fasesAtivas.isEmpty()) {
            status = StatusCronograma.VAZIO;
        } else if (fasesAtivas.stream().allMatch(f -> f.getStatus() == StatusFase.CONCLUIDA)) {
            status = StatusCronograma.CONCLUIDO;
        } else if (fasesAtivas.stream().anyMatch(f -> f.getStatus() != StatusFase.NAO_INICIADA)) {
            status = StatusCronograma.EM_ANDAMENTO;
        } else {
            status = StatusCronograma.PLANEJADO;
        }
        dataUltimaAtualizacao = LocalDate.now();
    }

    /**
     * Método a ser chamado nos métodos que alteram o cronograma. Chama atualizarStatus()
     */
    public void registrarAtualizacao() {
        if(status != StatusCronograma.CANCELADO) {
            atualizarStatus();
        }
        else {
            dataUltimaAtualizacao = LocalDate.now();
        }
    }

    /**
     * Método para verificar se o cronograma pode ser alterado
     */
    public void checarEditabilidade() {
        if(status == StatusCronograma.CONCLUIDO || status == StatusCronograma.CANCELADO) {
            throw new IllegalStateException("Um cronograma " + status + " não pode ser alterado.");
        }
    }

    // ===== GETTERS =====
    
    public Long getId() {
        return id;
    }

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public StatusCronograma getStatus() {
        return status;
    }

    public List<Fase> getFases() {
        return Collections.unmodifiableList(fases);
    }

    public Obra getObra() {
        return obra;
    }    
}
