package br.com.obras.auditoria.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditavel")
@Getter
@NoArgsConstructor
public class Auditavel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "excluido", nullable = false)
    private boolean excluido;

    public static Auditavel novo() {
        return new Auditavel();
    }

    public void excluir() {
        this.excluido = true;
    }

    public RegistroAuditoria registrar(AcaoAuditoria acao, Long usuarioId, String detalhe) {
        return RegistroAuditoria.de(this, usuarioId, acao, detalhe);
    }
}
