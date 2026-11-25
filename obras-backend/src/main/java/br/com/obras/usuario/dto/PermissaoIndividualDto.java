package br.com.obras.usuario.dto;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;

public record PermissaoIndividualDto(ModuloSistema modulo, AcaoPermissao acao, boolean concedida) {}
