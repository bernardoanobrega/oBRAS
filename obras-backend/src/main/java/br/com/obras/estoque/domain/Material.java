package br.com.obras.estoque.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "material")
@Getter
@NoArgsConstructor
public class Material {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    public static Material cadastrar(String nome, String descricao, String unidadeMedida) {
        Material m = new Material();
        m.atualizar(nome, descricao, unidadeMedida);
        return m;
    }

    public void atualizar(String nome, String descricao, String unidadeMedida) {
        if (nome == null || nome.isBlank()) throw new RegraNegocioException("Nome do material é obrigatório");
        if (unidadeMedida == null || unidadeMedida.isBlank()) {
            throw new RegraNegocioException("Unidade de medida é obrigatória");
        }
        this.nome = nome.trim();
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida.trim();
    }
}
