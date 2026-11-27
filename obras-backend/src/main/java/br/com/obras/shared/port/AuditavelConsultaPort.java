package br.com.obras.shared.port;

import br.com.obras.auditoria.domain.Auditavel;

import java.util.Optional;

public interface AuditavelConsultaPort {
    Optional<Auditavel> buscar(Long auditavelId);
}
