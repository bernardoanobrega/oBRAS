package br.com.obras.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialRequest(@NotBlank @Size(max = 150) String nome,
                              @Size(max = 500) String descricao,
                              @NotBlank @Size(max = 20) String unidadeMedida) {}
