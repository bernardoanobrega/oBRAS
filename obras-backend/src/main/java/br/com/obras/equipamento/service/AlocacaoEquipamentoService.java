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
import br.com.obras.shared.port.ObraPort;
import br.com.obras.shared.port.UsuarioPort;
import br.com.obras.shared.port.UsuarioResumo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Alocação, devolução, transferência e prorrogação de equipamentos em obras.
 * A obra da rota é quem autoriza a operação: a alocação envolvida precisa ser dela.
 */
@Service
@RequiredArgsConstructor
public class AlocacaoEquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final AlocacaoEquipamentoRepository alocacaoRepository;
    private final ObraPort obraPort;
    private final UsuarioPort usuarioPort;

    @Transactional(readOnly = true)
    public List<AlocacaoEquipamentoDto> listarAtivasDaObra(Long obraId) {
        return alocacaoRepository.listarAtivasPorObra(obraId).stream()
                .map(AlocacaoEquipamentoDto::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlocacaoEquipamentoDto> listarHistorico(Long equipamentoId) {
        if (!equipamentoRepository.existsById(equipamentoId)) {
            throw new RecursoNaoEncontradoException("Equipamento", equipamentoId);
        }
        return alocacaoRepository.findByEquipamentoIdOrderByDataInicioDescIdDesc(equipamentoId).stream()
                .map(AlocacaoEquipamentoDto::de)
                .toList();
    }

    @Transactional
    public AlocacaoEquipamentoDto alocar(Long obraId, Long equipamentoId, AlocarEquipamentoDto dto) {
        Equipamento equipamento = buscarEquipamento(equipamentoId);
        exigirObraExistente(obraId);
        exigirResponsavelAtivo(dto.responsavelId());

        AlocacaoEquipamento alocacao = equipamento.alocar(
                obraId, dto.dataPrevistaDevolucao(), dto.responsavelId(), dto.observacao());
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
     * Move o equipamento da obra da rota para outra obra. O encerramento da alocação anterior é gravado
     * antes da nova alocação, para não violar o índice de alocação ativa única.
     */
    @Transactional
    public AlocacaoEquipamentoDto transferir(Long obraId, Long equipamentoId, TransferirEquipamentoDto dto) {
        Equipamento equipamento = buscarEquipamento(equipamentoId);
        equipamento.alocacaoAtiva().ifPresent(alocacao -> exigirAlocacaoDaObra(obraId, alocacao));
        exigirObraExistente(dto.obraDestinoId());
        exigirResponsavelAtivo(dto.responsavelId());

        AlocacaoEquipamento nova = equipamento.transferir(
                dto.obraDestinoId(), dto.dataPrevistaDevolucao(), dto.responsavelId(), dto.observacao());
        alocacaoRepository.flush();
        return AlocacaoEquipamentoDto.de(alocacaoRepository.save(nova));
    }

    @Transactional
    public AlocacaoEquipamentoDto prorrogar(Long obraId, Long alocacaoId, ProrrogarAlocacaoDto dto) {
        AlocacaoEquipamento alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocação", alocacaoId));
        exigirAlocacaoDaObra(obraId, alocacao);

        alocacao.prorrogar(dto.novaDataPrevista());
        return AlocacaoEquipamentoDto.de(alocacao);
    }

    private Equipamento buscarEquipamento(Long equipamentoId) {
        return equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento", equipamentoId));
    }

    private void exigirObraExistente(Long obraId) {
        obraPort.buscarResumo(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Obra", obraId));
    }

    private void exigirAlocacaoDaObra(Long obraId, AlocacaoEquipamento alocacao) {
        if (!alocacao.getObraId().equals(obraId)) {
            throw new RecursoNaoEncontradoException("Alocação", alocacao.getId());
        }
    }

    private void exigirResponsavelAtivo(Long responsavelId) {
        UsuarioResumo responsavel = usuarioPort.buscarResumo(responsavelId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável", responsavelId));
        if (!responsavel.estaAtivo()) {
            throw new RegraNegocioException("O responsável informado está inativo.");
        }
    }
}