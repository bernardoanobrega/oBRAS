package br.com.obras.obra.domain;

import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "alocacao_obra")
@Getter
@NoArgsConstructor
public class AlocacaoObra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "alocacao_obra_cargo",
        joinColumns = @JoinColumn(name = "alocacao_obra_id"),
        inverseJoinColumns = @JoinColumn(name = "cargo_id")
    )
    private Set<Cargo> cargos = new HashSet<>();

    public boolean estaAtiva() {
        return dataFim == null;
    }
}