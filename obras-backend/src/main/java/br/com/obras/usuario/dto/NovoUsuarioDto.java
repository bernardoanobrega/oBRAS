package br.com.obras.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF-02: regras de validação das credenciais. */
public record NovoUsuarioDto(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[A-Za-z0-9._@-]+$",
                message = "Login aceita letras, números e . _ @ -") String login,
        @NotBlank @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres") String senha) {}
