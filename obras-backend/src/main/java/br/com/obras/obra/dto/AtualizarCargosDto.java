package br.com.obras.obra.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AtualizarCargosDto(@NotEmpty List<@NotNull Long> cargoIds) {}
