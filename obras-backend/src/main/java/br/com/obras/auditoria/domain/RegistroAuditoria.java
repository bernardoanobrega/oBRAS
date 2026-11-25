package br.com.obras.auditoria.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_auditoria")
@Getter
@NoArgsConstructor
public class RegistroAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditavel_id", nullable = false)
    private Auditavel auditavel;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "acao", nullable = false, length = 10)
    private AcaoAuditoria acao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhe", columnDefinition = "jsonb")
    private String detalhe;

    public static RegistroAuditoria de(Auditavel auditavel, Long usuarioId, AcaoAuditoria acao, String detalhe) {
        RegistroAuditoria r = new RegistroAuditoria();
        r.auditavel = auditavel;
        r.usuarioId = usuarioId;
        r.acao = acao;
        r.dataHora = LocalDateTime.now();
        r.detalhe = detalhe;
        return r;
    }
}
