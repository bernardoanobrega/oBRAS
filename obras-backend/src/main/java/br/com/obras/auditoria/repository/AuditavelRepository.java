package br.com.obras.auditoria.repository;

import br.com.obras.auditoria.domain.Auditavel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditavelRepository extends JpaRepository<Auditavel, Long> {}
