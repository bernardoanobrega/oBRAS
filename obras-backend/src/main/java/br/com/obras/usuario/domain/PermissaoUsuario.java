package br.com.obras.usuario.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Exceção individual: concedida=true adiciona; concedida=false retira o que o cargo concederia. */
@Entity
@Table(name = "permissao_usuario")
@Getter
@NoArgsConstructor
public class PermissaoUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "permissao_id")
    private Permissao permissao;

    @Column(nullable = false)
    private boolean concedida;

    PermissaoUsuario(Usuario usuario, Permissao permissao, boolean concedida) {
        this.usuario = usuario;
        this.permissao = permissao;
        this.concedida = concedida;
    }

    void atualizar(boolean concedida) { this.concedida = concedida; }

    public void revogar() { this.concedida = false; }
}
