package br.com.obras.usuario.dto;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;
import jakarta.validation.constraints.NotNull;

public record DefinirPermissaoDto(@NotNull ModuloSistema modulo, @NotNull AcaoPermissao acao, @NotNull Boolean concedida) {}
