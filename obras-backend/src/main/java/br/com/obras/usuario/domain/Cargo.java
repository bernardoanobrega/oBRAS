package br.com.obras.usuario.domain;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cargo")
@Getter
@NoArgsConstructor
public class Cargo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cargo_permissao",
            joinColumns = @JoinColumn(name = "cargo_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id"))
    private Set<Permissao> permissoes = new HashSet<>();

    public Cargo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public void adicionarPermissao(Permissao p) { permissoes.add(p); }
    public void removerPermissao(Permissao p) { permissoes.remove(p); }

    public boolean possuiPermissao(ModuloSistema modulo, AcaoPermissao acao) {
        return permissoes.stream().anyMatch(p -> p.getModulo() == modulo && p.getAcao() == acao);
    }
}
