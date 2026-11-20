package br.com.obras.diario.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "paralisacao")
@Getter
@NoArgsConstructor
public class Paralisacao {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Ocorrencia ocorrencia;

    @Column(name = "inicio_paralisacao", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fim_paralisacao")
    private LocalDateTime fim;

    public static Paralisacao criar(Ocorrencia ocorrencia, LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        Paralisacao p = new Paralisacao();
        p.ocorrencia = ocorrencia;
        p.inicio = inicio;
        p.fim = fim;
        return p;
    }

    public void editar(LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        this.inicio = inicio;
        this.fim = fim;
    }

    private static void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null) throw new RegraNegocioException("Início da paralisação é obrigatório");
        if (fim != null && fim.isBefore(inicio)) {
            throw new RegraNegocioException("Fim da paralisação não pode ser antes do início");
        }
    }
}
