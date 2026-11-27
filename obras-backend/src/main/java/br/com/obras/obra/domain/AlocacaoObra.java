package br.com.obras.obra.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
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

    public static AlocacaoObra criar(Obra obra, Usuario usuario, LocalDate dataInicio, Set<Cargo> cargos) {
        validar(obra, usuario, dataInicio, null, cargos);
        AlocacaoObra alocacao = new AlocacaoObra();
        alocacao.obra = obra;
        alocacao.usuario = usuario;
        alocacao.dataInicio = dataInicio;
        alocacao.cargos.addAll(cargos);
        return alocacao;
    }

    public static AlocacaoObra criarEncerrada(Obra obra, Usuario usuario, LocalDate dataInicio,
                                              LocalDate dataFim, Set<Cargo> cargos) {
        validar(obra, usuario, dataInicio, dataFim, cargos);
        AlocacaoObra alocacao = criar(obra, usuario, dataInicio, cargos);
        alocacao.dataFim = dataFim;
        return alocacao;
    }

    public boolean estaAtiva() {
        return dataFim == null;
    }

    public void encerrar(LocalDate dataFim) {
        if (!estaAtiva()) {
            throw new RegraNegocioException("A alocação já está encerrada");
        }
        if (dataFim == null || dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de encerramento não pode ser anterior ao início");
        }
        this.dataFim = dataFim;
    }

    public void substituirCargos(Set<Cargo> cargos) {
        if (cargos == null || cargos.isEmpty()) {
            throw new RegraNegocioException("A alocação exige ao menos um cargo");
        }
        this.cargos.clear();
        this.cargos.addAll(cargos);
    }

    private static void validar(Obra obra, Usuario usuario, LocalDate inicio,
                                LocalDate fim, Set<Cargo> cargos) {
        if (obra == null || usuario == null) {
            throw new RegraNegocioException("A alocação exige obra e usuário");
        }
        if (inicio == null) {
            throw new RegraNegocioException("A data de início é obrigatória");
        }
        if (fim != null && fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início");
        }
        if (cargos == null || cargos.isEmpty()) {
            throw new RegraNegocioException("A alocação exige ao menos um cargo");
        }
        if (cargos.stream().anyMatch(Objects::isNull)) {
            throw new RegraNegocioException("A alocação contém cargo inválido");
        }
    }

    /**
     * Igualdade por id: duas alocações são a mesma se têm o mesmo id do banco.
     * Sem id (ainda não salva), só é igual a si mesma. Compara a classe real
     * para que um proxy do Hibernate seja igual à entidade que ele representa.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || classeReal(this) != classeReal(o)) return false;
        Long outroId = ((AlocacaoObra) o).getId();
        return id != null && id.equals(outroId);
    }

    /** Constante por classe: não muda quando o banco atribui o id no INSERT. */
    @Override
    public int hashCode() {
        return classeReal(this).hashCode();
    }

    private static Class<?> classeReal(Object o) {
        return o instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    }
}