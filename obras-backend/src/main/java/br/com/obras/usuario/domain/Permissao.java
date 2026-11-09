package br.com.obras.usuario.domain;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissao")
@Getter
@NoArgsConstructor
public class Permissao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ModuloSistema modulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AcaoPermissao acao;

    public Permissao(ModuloSistema modulo, AcaoPermissao acao) {
        this.modulo = modulo;
        this.acao = acao;
    }
}
