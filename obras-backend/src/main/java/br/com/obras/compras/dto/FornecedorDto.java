package br.com.obras.compras.dto;

import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.compras.domain.SituacaoFornecedor;

public record FornecedorDto(Long id, String nomeRazaoSocial, String cpfCnpj, String email, String telefone,
                            SituacaoFornecedor situacao) {
    public static FornecedorDto de(Fornecedor f) {
        return new FornecedorDto(f.getId(), f.getNomeRazaoSocial(), f.getCpfCnpj(), f.getEmail(),
                f.getTelefone(), f.getSituacao());
    }
}
