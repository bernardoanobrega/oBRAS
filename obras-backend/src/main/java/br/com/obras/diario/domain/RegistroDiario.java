package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "registro_diario")
@Getter
@NoArgsConstructor
public class RegistroDiario {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Auditavel auditavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "clima", nullable = false, length = 15)
    private Clima clima;

    @Column(name = "descricao", length = 2000)
    private String descricao;

    @Column(name = "excluido", nullable = false)
    private boolean excluido;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "registro_diario_equipe",
        joinColumns = @JoinColumn(name = "registro_diario_id"),
        inverseJoinColumns = @JoinColumn(name = "alocacao_obra_id")
    )
    private Set<AlocacaoObra> equipePresente = new HashSet<>();

    public static RegistroDiario criar(
        Auditavel auditavel,
        Obra obra,
        LocalDate data,
        Clima clima,
        String descricao,
        Collection<AlocacaoObra> equipePresente
    ) {
        RegistroDiario d = new RegistroDiario();
        d.auditavel = auditavel;
        d.obra = obra;
        d.data = data;
        d.clima = clima;
        d.descricao = descricao;
        d.excluido = false;
        d.equipePresente.addAll(equipePresente);
        return d;
    }

    public void editar(Clima clima, String descricao, Collection<AlocacaoObra> equipePresente) {
        this.clima = clima;
        this.descricao = descricao;
        this.equipePresente.clear();
        this.equipePresente.addAll(equipePresente);
    }

    public void excluirLogico() {
        this.excluido = true;
    }
}
