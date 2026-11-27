package br.com.obras.auditoria.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.domain.RegistroAuditoria;
import br.com.obras.auditoria.repository.AuditavelRepository;
import br.com.obras.auditoria.repository.RegistroAuditoriaRepository;
import br.com.obras.shared.port.AuditavelConsultaPort;
import br.com.obras.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuditoriaService implements AuditavelConsultaPort {
    private final AuditavelRepository auditaveis;
    private final RegistroAuditoriaRepository registros;

    public AuditoriaService(AuditavelRepository auditaveis, RegistroAuditoriaRepository registros) {
        this.auditaveis = auditaveis;
        this.registros = registros;
    }

    public Auditavel criar(AcaoAuditoria acao, String detalhe) {
        Auditavel auditavel = auditaveis.saveAndFlush(Auditavel.novo());
        registros.save(RegistroAuditoria.de(auditavel, CurrentUser.id(), acao, detalhe));
        return auditavel;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Auditavel> buscar(Long auditavelId) {
        return auditaveis.findById(auditavelId).filter(a -> !a.isExcluido());
    }

    public void registrar(Auditavel auditavel, AcaoAuditoria acao, String detalhe) {
        registros.save(RegistroAuditoria.de(auditavel, CurrentUser.id(), acao, detalhe));
    }

    public void registrar(RegistroAuditoria registro) {
        registros.save(registro);
    }

    public void excluir(Auditavel auditavel) {
        auditavel.excluir();
        registros.save(RegistroAuditoria.de(auditavel, CurrentUser.id(), AcaoAuditoria.EXCLUSAO, null));
    }
}
