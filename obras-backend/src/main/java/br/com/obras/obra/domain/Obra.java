package br.com.obras.obra.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "obra")
@Getter
@NoArgsConstructor
public class Obra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipologia tipologia;

    @Column(nullable = false, length = 300)
    private String endereco;

    @Column(name = "inicio_previsto", nullable = false)
    private LocalDate inicioPrevisto;

    @Column(name = "fim_previsto", nullable = false)
    private LocalDate fimPrevisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusObra status;

    @Column(length = 50)
    private String art;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    @Column(name = "auditavel_id", nullable = false)
    private Long auditavelId;

    @Transient
    private Auditavel auditavel;

    private Obra(
            String nome,
            String descricao,
            Tipologia tipologia,
            String endereco,
            LocalDate inicioPrevisto,
            LocalDate fimPrevisto,
            String art,
            Long responsavelId,
            Auditavel auditavel
    ) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipologia = tipologia;
        this.endereco = endereco;
        this.inicioPrevisto = inicioPrevisto;
        this.fimPrevisto = fimPrevisto;
        this.status = StatusObra.PLANEJADA;
        this.art = art;
        this.responsavelId = responsavelId;
        this.auditavel = auditavel;
        this.auditavelId = auditavel.getId();
    }

    public static Obra cadastrar(
            String nome,
            String descricao,
            Tipologia tipologia,
            String endereco,
            LocalDate inicioPrevisto,
            LocalDate fimPrevisto,
            String art,
            Long responsavelId,
            Auditavel auditavel
    ) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da obra é obrigatório");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new RegraNegocioException("O endereço da obra é obrigatório");
        }
        if (tipologia == null) {
            throw new RegraNegocioException("A tipologia da obra é obrigatória");
        }
        if (inicioPrevisto == null || fimPrevisto == null) {
            throw new RegraNegocioException("O período previsto da obra é obrigatório");
        }
        if (fimPrevisto.isBefore(inicioPrevisto)) {
            throw new RegraNegocioException("A data de fim previsto não pode ser anterior ao início previsto");
        }
        if (auditavel == null || auditavel.isExcluido()) {
            throw new RegraNegocioException("A obra exige um registro auditável ativo");
        }
        return new Obra(nome, descricao, tipologia, endereco, inicioPrevisto, fimPrevisto,
                art, responsavelId, auditavel);
    }
}
