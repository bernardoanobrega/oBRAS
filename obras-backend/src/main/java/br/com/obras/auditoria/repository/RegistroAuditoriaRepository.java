package br.com.obras.auditoria.repository;

import br.com.obras.auditoria.domain.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {}
