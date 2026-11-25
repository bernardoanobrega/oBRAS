package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ocorrencia")
@Getter
@NoArgsConstructor
public class Ocorrencia {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Auditavel auditavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_diario_id", nullable = false)
    private RegistroDiario registroDiario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alocacao_obra_id", nullable = false)
    private AlocacaoObra alocacaoObra;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 15)
    private CategoriaOcorrencia categoria;

    @Column(name = "descricao", nullable = false, length = 2000)
    private String descricao;

    @OneToOne(mappedBy = "ocorrencia", fetch = FetchType.LAZY)
    private Paralisacao paralisacao;

    @OneToMany(mappedBy = "ocorrencia", fetch = FetchType.LAZY)
    private List<RegistroFotografico> fotos = new ArrayList<>();

    public static Ocorrencia criar(
        Auditavel auditavel,
        RegistroDiario registroDiario,
        AlocacaoObra alocacaoObra,
        LocalDateTime dataHora,
        CategoriaOcorrencia categoria,
        String descricao
    ) {
        if (registroDiario == null) {
            throw new RegraNegocioException("Ocorrência precisa estar vinculada a um diário");
        }
        if (alocacaoObra == null) {
            throw new RegraNegocioException("Autor da ocorrência é obrigatório");
        }
        exigirCategoria(categoria);
        exigirDescricao(descricao);
        LocalDateTime momento = dataHora == null ? LocalDateTime.now() : dataHora;
        exigirMesmoDiaDoDiario(registroDiario, momento);
        Ocorrencia o = new Ocorrencia();
        o.auditavel = auditavel;
        o.registroDiario = registroDiario;
        o.alocacaoObra = alocacaoObra;
        o.dataHora = momento;
        o.categoria = categoria;
        o.descricao = descricao;
        return o;
    }

    public void editar(LocalDateTime dataHora, CategoriaOcorrencia categoria, String descricao) {
        exigirCategoria(categoria);
        exigirDescricao(descricao);
        LocalDateTime momento = dataHora == null ? this.dataHora : dataHora;
        exigirMesmoDiaDoDiario(this.registroDiario, momento);
        this.dataHora = momento;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    /**
     * ADR-18: a ocorrência pertence ao diário de um dia, então a data dela é a
     * desse dia. Sem padrão especial: se a dataHora não vier, vale "agora", e no
     * diário de um dia passado isso é recusado; quem registra atrasado informa o horário.
     */
    private static void exigirMesmoDiaDoDiario(RegistroDiario diario, LocalDateTime momento) {
        if (!momento.toLocalDate().equals(diario.getData())) {
            throw new RegraNegocioException("A ocorrência deve ser do mesmo dia do diário");
        }
    }

    public void definirParalisacao(Paralisacao paralisacao) {
        this.paralisacao = paralisacao;
    }

    private static void exigirCategoria(CategoriaOcorrencia categoria) {
        if (categoria == null) {
            throw new RegraNegocioException("Categoria da ocorrência é obrigatória");
        }
    }

    private static void exigirDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descrição da ocorrência é obrigatória");
        }
    }
}
