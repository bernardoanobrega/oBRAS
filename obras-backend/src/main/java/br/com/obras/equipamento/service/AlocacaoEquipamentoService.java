package br.com.obras.equipamento.service;

import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.dto.AlocacaoEquipamentoDto;
import br.com.obras.equipamento.dto.AlocarEquipamentoDto;
import br.com.obras.equipamento.dto.DevolverEquipamentoDto;
import br.com.obras.equipamento.dto.ProrrogarAlocacaoDto;
import br.com.obras.equipamento.dto.TransferirEquipamentoDto;
import br.com.obras.equipamento.repository.AlocacaoEquipamentoRepository;
import br.com.obras.equipamento.repository.EquipamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.CanteiroPort;
import br.com.obras.shared.port.CanteiroResumo;
import br.com.obras.shared.port.UsuarioPort;
import br.com.obras.shared.port.UsuarioResumo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alocação, devolução, transferência e prorrogação de equipamentos nos canteiros de uma obra.
 * A obra da rota é quem autoriza a operação: o canteiro envolvido precisa pertencer a ela.
 */
@Service
@RequiredArgsConstructor
public class AlocacaoEquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final AlocacaoEquipamentoRepository alocacaoRepository;
    private final CanteiroPort canteiroPort;
    private final UsuarioPort usuarioPort;

    @Transactional(readOnly = true)
    public List<AlocacaoEquipamentoDto> listarAtivasDaObra(Long obraId) {
        List<Long> canteiroIds = canteiroPort.listarIdsPorObra(obraId);
        if (canteiroIds.isEmpty()) {
            return List.of();
        }
        return alocacaoRepository.listarAtivasPorCanteiros(canteiroIds).stream()
                .map(AlocacaoEquipamentoDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlocacaoEquipamentoDto> listarHistorico(Long equipamentoId) {
        if (!equipamentoRepository.existsById(equipamentoId)) {
            throw new RecursoNaoEncontradoException("Equipamento não encontrado.");
        }
        return alocacaoRepository.findByEquipamentoIdOrderByDataInicioDescIdDesc(equipamentoId).stream()
                .map(AlocacaoEquipamentoDto::de)
                .toList();
    }

    @Transactional
    public AlocacaoEquipamentoDto alocar(Long obraId, Long equipamentoId, AlocarEquipamentoDto dto) {
        Equipamento equipamento = buscarEquipamento(equipamentoId);
        exigirCanteiroDaObra(obraId, dto.canteiroId());
        exigirResponsavelAtivo(dto.responsavelId());

        AlocacaoEquipamento alocacao = equipamento.alocar(
                dto.canteiroId(), dto.dataPrevistaDevolucao(), dto.responsavelId(), dto.observacao());
        return AlocacaoEquipamentoDto.de(alocacaoRepository.save(alocacao));
    }

    @Transactional
    public AlocacaoEquipamentoDto devolver(Long obraId, Long equipamentoId, DevolverEquipamentoDto dto) {
        Equipamento equipamento = buscarEquipamento(equipamentoId);
        Optional<AlocacaoEquipamento> alocacaoAtiva = equipamento.alocacaoAtiva();
        alocacaoAtiva.ifPresent(alocacao -> exigirAlocacaoDaObra(obraId, alocacao));

        equipamento.devolver(dto.observacao());
        return AlocacaoEquipamentoDto.de(alocacaoAtiva.orElseThrow());
    }

    /**
     * A transferência é permitida apenas entre canteiros da mesma obra. O encerramento da alocação
     * anterior é gravado antes da nova alocação, para não violar o índice de alocação ativa única.
     */
    @Transactional
    public AlocacaoEquipamentoDto transferir(Long obraId, Long equipamentoId, TransferirEquipamentoDto dto) {
        Equipamento equipamento = buscarEquipamento(equipamentoId);
        equipamento.alocacaoAtiva().ifPresent(alocacao -> exigirAlocacaoDaObra(obraId, alocacao));
        exigirCanteiroDaObra(obraId, dto.canteiroDestinoId());
        exigirResponsavelAtivo(dto.responsavelId());

        AlocacaoEquipamento nova = equipamento.transferir(
                dto.canteiroDestinoId(), dto.dataPrevistaDevolucao(), dto.responsavelId(), dto.observacao());
        alocacaoRepository.flush();
        return AlocacaoEquipamentoDto.de(alocacaoRepository.save(nova));
    }

    @Transactional
    public AlocacaoEquipamentoDto prorrogar(Long obraId, Long alocacaoId, ProrrogarAlocacaoDto dto) {
        AlocacaoEquipamento alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocação não encontrada nesta obra."));
        exigirAlocacaoDaObra(obraId, alocacao);

        alocacao.prorrogar(dto.novaDataPrevista());
        return AlocacaoEquipamentoDto.de(alocacao);
    }

    private Equipamento buscarEquipamento(Long equipamentoId) {
        return equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento não encontrado."));
    }

    private void exigirCanteiroDaObra(Long obraId, Long canteiroId) {
        CanteiroResumo canteiro = canteiroPort.buscarResumo(canteiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Canteiro não encontrado."));
        if (!canteiro.obraId().equals(obraId)) {
            throw new RegraNegocioException("O canteiro informado não pertence à obra.");
        }
    }

    private void exigirAlocacaoDaObra(Long obraId, AlocacaoEquipamento alocacao) {
        canteiroPort.buscarResumo(alocacao.getCanteiroId())
                .filter(canteiro -> canteiro.obraId().equals(obraId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocação não encontrada nesta obra."));
    }

    private void exigirResponsavelAtivo(Long responsavelId) {
        UsuarioResumo responsavel = usuarioPort.buscarResumo(responsavelId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável não encontrado."));
        if (!responsavel.estaAtivo()) {
            throw new RegraNegocioException("O responsável informado está inativo.");
        }
    }
}