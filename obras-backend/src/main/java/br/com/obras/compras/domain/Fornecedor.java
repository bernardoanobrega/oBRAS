package br.com.obras.compras.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fornecedor")
@Getter
@NoArgsConstructor
public class Fornecedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_razao_social", nullable = false, length = 200)
    private String nomeRazaoSocial;

    @Column(name = "cpf_cnpj", nullable = false, length = 18)
    private String cpfCnpj;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoFornecedor situacao;

    public static Fornecedor cadastrar(String nomeRazaoSocial, String cpfCnpj, String email, String telefone) {
        Fornecedor f = new Fornecedor();
        f.aplicarDados(nomeRazaoSocial, cpfCnpj, email, telefone);
        f.situacao = SituacaoFornecedor.ATIVO;
        return f;
    }

    public void alterarDados(String nomeRazaoSocial, String cpfCnpj, String email, String telefone) {
        aplicarDados(nomeRazaoSocial, cpfCnpj, email, telefone);
    }

    /** Fornecedor nunca é excluído: o histórico de compras é preservado. */
    public void desativar() { this.situacao = SituacaoFornecedor.INATIVO; }

    public boolean estaAtivo() { return situacao == SituacaoFornecedor.ATIVO; }

    /** Somente dígitos; CPF tem 11 e CNPJ tem 14. */
    public static String normalizarDocumento(String cpfCnpj) {
        String digitos = cpfCnpj == null ? "" : cpfCnpj.replaceAll("\\D", "");
        if (digitos.length() != 11 && digitos.length() != 14) {
            throw new RegraNegocioException("CPF/CNPJ inválido: informe 11 ou 14 dígitos");
        }
        return digitos;
    }

    private void aplicarDados(String nomeRazaoSocial, String cpfCnpj, String email, String telefone) {
        if (nomeRazaoSocial == null || nomeRazaoSocial.isBlank()) {
            throw new RegraNegocioException("Nome ou razão social é obrigatório");
        }
        this.nomeRazaoSocial = nomeRazaoSocial.trim();
        this.cpfCnpj = normalizarDocumento(cpfCnpj);
        this.email = email;
        this.telefone = telefone;
    }
}
