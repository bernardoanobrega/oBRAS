package br.com.obras.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioDto(@NotBlank @Size(max = 150) String nome) {}
