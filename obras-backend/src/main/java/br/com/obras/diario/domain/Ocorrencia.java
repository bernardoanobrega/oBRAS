package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.AlocacaoObra;
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
        Ocorrencia o = new Ocorrencia();
        o.auditavel = auditavel;
        o.registroDiario = registroDiario;
        o.alocacaoObra = alocacaoObra;
        o.dataHora = dataHora == null ? LocalDateTime.now() : dataHora;
        o.categoria = categoria;
        o.descricao = descricao;
        return o;
    }

    public void editar(LocalDateTime dataHora, CategoriaOcorrencia categoria, String descricao) {
        this.dataHora = dataHora == null ? this.dataHora : dataHora;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public void definirParalisacao(Paralisacao paralisacao) {
        this.paralisacao = paralisacao;
    }
}
