package br.com.obras.diario.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ParalisacaoDto(@NotNull LocalDateTime inicio, LocalDateTime fim) {}
