package br.com.obras.obra.domain;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.domain.RegistroAuditoria;
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
        validarParametros(nome, tipologia, endereco, inicioPrevisto, fimPrevisto);
        if (auditavel == null || auditavel.isExcluido()) {
            throw new RegraNegocioException("A obra exige um registro auditável ativo");
        }
        return new Obra(nome, descricao, tipologia, endereco, inicioPrevisto, fimPrevisto,
                art, responsavelId, auditavel);
    }

    public RegistroAuditoria iniciar(boolean responsavelAtivo, Long usuarioId) {
        exigirAlteravel();
        if (status != StatusObra.PLANEJADA) {
            throw new RegraNegocioException("Somente obra planejada pode ser iniciada");
        }
        if (responsavelId == null) {
            throw new RegraNegocioException("A obra exige um responsável para ser iniciada");
        }
        if (!responsavelAtivo) {
            throw new RegraNegocioException("O responsável da obra precisa estar ativo para iniciá-la");
        }
        return mudarStatus(StatusObra.EM_ANDAMENTO, usuarioId);
    }

    public RegistroAuditoria paralisar(Long usuarioId) {
        exigirAlteravel();
        if (status != StatusObra.EM_ANDAMENTO) {
            throw new RegraNegocioException("Somente obra em andamento pode ser paralisada");
        }
        return mudarStatus(StatusObra.PARALISADA, usuarioId);
    }

    public RegistroAuditoria retomar(Long usuarioId) {
        exigirAlteravel();
        if (status != StatusObra.PARALISADA) {
            throw new RegraNegocioException("Somente obra paralisada pode ser retomada");
        }
        return mudarStatus(StatusObra.EM_ANDAMENTO, usuarioId);
    }

    public RegistroAuditoria concluir(Long usuarioId) {
        exigirAlteravel();
        if (status != StatusObra.EM_ANDAMENTO) {
            throw new RegraNegocioException("Somente obra em andamento pode ser concluída");
        }
        return mudarStatus(StatusObra.CONCLUIDA, usuarioId);
    }

    public RegistroAuditoria cancelar(Long usuarioId) {
        exigirAlteravel();
        return mudarStatus(StatusObra.CANCELADA, usuarioId);
    }

    public RegistroAuditoria definirResponsavel(Long responsavelId, Long usuarioId) {
        exigirAlteravel();
        Long anterior = this.responsavelId;
        this.responsavelId = responsavelId;
        String detalhe = String.format("{\"responsavelAnterior\":%s,\"responsavelNovo\":%s}",
                anterior, responsavelId);
        return auditavel.registrar(AcaoAuditoria.EDICAO, usuarioId, detalhe);
    }

    public RegistroAuditoria atualizarParametros(
            String nome,
            String descricao,
            Tipologia tipologia,
            String endereco,
            LocalDate inicioPrevisto,
            LocalDate fimPrevisto,
            String art,
            Long usuarioId
    ) {
        exigirAlteravel();
        validarParametros(nome, tipologia, endereco, inicioPrevisto, fimPrevisto);
        this.nome = nome;
        this.descricao = descricao;
        this.tipologia = tipologia;
        this.endereco = endereco;
        this.inicioPrevisto = inicioPrevisto;
        this.fimPrevisto = fimPrevisto;
        this.art = art;
        return auditavel.registrar(AcaoAuditoria.EDICAO, usuarioId, "{\"campos\":\"parametros\"}");
    }

    private RegistroAuditoria mudarStatus(StatusObra novo, Long usuarioId) {
        StatusObra anterior = this.status;
        this.status = novo;
        String detalhe = String.format("{\"statusAnterior\":\"%s\",\"statusNovo\":\"%s\"}", anterior, novo);
        return auditavel.registrar(AcaoAuditoria.EDICAO, usuarioId, detalhe);
    }

    private void exigirAlteravel() {
        if (auditavel == null || auditavel.isExcluido()) {
            throw new RegraNegocioException("A obra exige um registro auditável ativo");
        }
        if (status == StatusObra.CONCLUIDA || status == StatusObra.CANCELADA) {
            throw new RegraNegocioException("Obra concluída ou cancelada não pode ser alterada");
        }
    }

    private static void validarParametros(
            String nome,
            Tipologia tipologia,
            String endereco,
            LocalDate inicioPrevisto,
            LocalDate fimPrevisto
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
    }
}
