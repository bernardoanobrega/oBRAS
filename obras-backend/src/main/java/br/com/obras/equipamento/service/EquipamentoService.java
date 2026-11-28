package br.com.obras.equipamento.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import br.com.obras.equipamento.dto.EquipamentoCriacaoDto;
import br.com.obras.equipamento.dto.EquipamentoDto;
import br.com.obras.equipamento.dto.EquipamentoEdicaoDto;
import br.com.obras.equipamento.repository.EquipamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cadastro global de equipamentos (não depende de obra). Toda mudança gera registro de auditoria. */
@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Page<EquipamentoDto> listar(StatusEquipamento status, TipoEquipamento tipo, String busca,
            Pageable pageable) {
        String termo = busca == null ? "" : busca.strip();
        return equipamentoRepository.buscar(status, tipo, termo, pageable).map(EquipamentoDto::de);
    }

    @Transactional(readOnly = true)
    public EquipamentoDto buscar(Long id) {
        return EquipamentoDto.de(buscarEntidade(id));
    }

    @Transactional
    public EquipamentoDto cadastrar(EquipamentoCriacaoDto dto) {
        if (dto.codigoPatrimonio() != null
                && equipamentoRepository.existsByCodigoPatrimonio(dto.codigoPatrimonio().strip())) {
            throw new RegraNegocioException("Já existe um equipamento com este código de patrimônio.");
        }
        Auditavel auditavel = auditoriaService.criar(
                AcaoAuditoria.CRIACAO, DetalhesAuditoriaEquipamento.cadastro(dto.tipo()));
        var equipamento = new Equipamento(auditavel, dto.codigoPatrimonio(), dto.descricao(), dto.tipo());
        return EquipamentoDto.de(equipamentoRepository.save(equipamento));
    }

    @Transactional
    public EquipamentoDto editar(Long id, EquipamentoEdicaoDto dto) {
        Equipamento equipamento = buscarEntidade(id);
        equipamento.editar(dto.descricao(), dto.tipo());
        auditoriaService.registrar(equipamento.getAuditavel(), AcaoAuditoria.EDICAO,
                DetalhesAuditoriaEquipamento.edicao());
        return EquipamentoDto.de(equipamento);
    }

    @Transactional
    public EquipamentoDto desativar(Long id) {
        Equipamento equipamento = buscarEntidade(id);
        StatusEquipamento anterior = equipamento.getStatus();
        equipamento.desativar();
        registrarMudancaDeStatus(equipamento, anterior);
        return EquipamentoDto.de(equipamento);
    }

    @Transactional
    public EquipamentoDto reativar(Long id) {
        Equipamento equipamento = buscarEntidade(id);
        StatusEquipamento anterior = equipamento.getStatus();
        equipamento.reativar();
        registrarMudancaDeStatus(equipamento, anterior);
        return EquipamentoDto.de(equipamento);
    }

    private void registrarMudancaDeStatus(Equipamento equipamento, StatusEquipamento anterior) {
        auditoriaService.registrar(equipamento.getAuditavel(), AcaoAuditoria.EDICAO,
                DetalhesAuditoriaEquipamento.mudancaDeStatus(anterior, equipamento.getStatus()));
    }

    private Equipamento buscarEntidade(Long id) {
        return equipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento", id));
    }
}