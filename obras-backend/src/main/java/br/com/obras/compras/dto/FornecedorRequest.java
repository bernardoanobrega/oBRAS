package br.com.obras.compras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank @Size(max = 200) String nomeRazaoSocial,
        @NotBlank @Size(max = 18) String cpfCnpj,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefone) {}
