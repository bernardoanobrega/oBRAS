package br.com.obras.usuario.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoUsuario situacao;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Embedded
    private Credencial credencial;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PermissaoUsuario> permissoes = new ArrayList<>();

    public static Usuario cadastrar(String nome, String login, String senhaHash) {
        Usuario u = new Usuario();
        u.nome = nome;
        u.credencial = new Credencial(login, senhaHash);
        u.situacao = SituacaoUsuario.ATIVO;
        u.dataCadastro = LocalDateTime.now();
        return u;
    }

    public void atualizarDados(String nome) {
        if (nome == null || nome.isBlank()) throw new RegraNegocioException("Nome é obrigatório");
        this.nome = nome;
    }

    /** Usuário nunca é apagado fisicamente. */
    public void desativar() { this.situacao = SituacaoUsuario.INATIVO; }

    public boolean estaAtivo() { return situacao == SituacaoUsuario.ATIVO; }

    /** Cria ou atualiza (upsert) a exceção individual para a permissão. */
    public PermissaoUsuario definirPermissao(Permissao permissao, boolean concedida) {
        for (PermissaoUsuario pu : permissoes) {
            if (pu.getPermissao().getModulo() == permissao.getModulo()
                    && pu.getPermissao().getAcao() == permissao.getAcao()) {
                pu.atualizar(concedida);
                return pu;
            }
        }
        PermissaoUsuario novo = new PermissaoUsuario(this, permissao, concedida);
        permissoes.add(novo);
        return novo;
    }
}
