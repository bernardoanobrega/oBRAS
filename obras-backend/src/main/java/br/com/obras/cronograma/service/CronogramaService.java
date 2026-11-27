package br.com.obras.cronograma.service;

import br.com.obras.cronograma.domain.AvancoFisico;
import br.com.obras.cronograma.domain.Cronograma;
import br.com.obras.cronograma.domain.Fase;
import br.com.obras.cronograma.dto.AvancoFisicoDto;
import br.com.obras.cronograma.dto.ConcluirFaseDto;
import br.com.obras.cronograma.dto.CronogramaDto;
import br.com.obras.cronograma.dto.CronogramaResumoDto;
import br.com.obras.cronograma.dto.FaseDto;
import br.com.obras.cronograma.dto.NovaFaseDto;
import br.com.obras.cronograma.dto.NovoAvancoFisicoDto;
import br.com.obras.cronograma.repository.CronogramaRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CronogramaService {
    private final CronogramaRepository cronogramas;
    private final AutorizacaoPort autorizacao;

    public CronogramaService(CronogramaRepository cronogramas, AutorizacaoPort autorizacao) {
        this.cronogramas = cronogramas;
        this.autorizacao = autorizacao;
    }

    /** 
     * Cria o cronograma da obra
     */
    @Transactional
    public CronogramaDto criar(Long obraId) {
        if (cronogramas.existsByObraId(obraId)) {
            throw new RegraNegocioException("Esta obra já possui cronograma");
        }
        Cronograma cronograma = cronogramas.save(Cronograma.criar(obraId));
        return CronogramaDto.de(cronograma, LocalDate.now());
    }

    /** 
     * Painel do cronograma: indicadores e linha do tempo das fases 
     */
    @Transactional(readOnly = true)
    public CronogramaDto buscarPorObra(Long obraId) {
        return CronogramaDto.de(obterPorObra(obraId), LocalDate.now());
    }

    /** 
     * Cronogramas das obras em que o usuário pode visualizar o módulo Cronograma
     */
    @Transactional(readOnly = true)
    public List<CronogramaResumoDto> listarVisiveis(Long usuarioId) {
        return cronogramas.findAllByOrderByObraIdAsc().stream()
                .filter(cronograma -> autorizacao.possuiPermissao(usuarioId, cronograma.getObraId(),
                        ModuloSistema.CRONOGRAMA, AcaoPermissao.VISUALIZAR))
                .map(CronogramaResumoDto::de)
                .toList();
    }

    @Transactional
    public CronogramaDto cancelar(Long obraId) {
        Cronograma cronograma = obterPorObra(obraId);
        cronograma.cancelarCronograma();
        return CronogramaDto.de(cronograma, LocalDate.now());
    }

    @Transactional
    public FaseDto adicionarFase(Long obraId, NovaFaseDto dto) {
        Fase fase = obterPorObra(obraId).adicionarFase(dto.nome(), dto.descricao(), dto.ordem(),
                dto.inicioPrevisto(), dto.fimPrevisto());
        cronogramas.flush();
        return FaseDto.de(fase, LocalDate.now());
    }

    @Transactional
    public void removerFase(Long obraId, Long faseId) {
        obterPorObra(obraId).removerFase(faseId);
    }

    @Transactional
    public FaseDto cancelarFase(Long obraId, Long faseId) {
        Fase fase = obterPorObra(obraId).buscarFase(faseId);
        fase.cancelarFase();
        return FaseDto.de(fase, LocalDate.now());
    }

    /** 
     * Registra o avanço físico acumulado
     */
    @Transactional
    public AvancoFisicoDto registrarAvanco(Long obraId, Long faseId, NovoAvancoFisicoDto dto, Long usuarioId) {
        Fase fase = obterPorObra(obraId).buscarFase(faseId);
        AvancoFisico avanco = fase.registrarAvanco(dto.percentualAcumulado().doubleValue(), dto.observacao(),
                usuarioId);
        cronogramas.flush();
        return AvancoFisicoDto.de(avanco);
    }

    @Transactional(readOnly = true)
    public List<AvancoFisicoDto> listarAvancos(Long obraId, Long faseId) {
        return obterPorObra(obraId).buscarFase(faseId).getAvancos().stream()
                .map(AvancoFisicoDto::de)
                .toList();
    }

    @Transactional
    public FaseDto concluirFase(Long obraId, Long faseId, ConcluirFaseDto dto) {
        Fase fase = obterPorObra(obraId).buscarFase(faseId);
        fase.concluir(dto.dataConclusao());
        return FaseDto.de(fase, LocalDate.now());
    }

    /** 
     * Fases atrasadas na data de referência, em ordem 
     */
    @Transactional(readOnly = true)
    public List<FaseDto> listarFasesAtrasadas(Long obraId, LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia == null ? LocalDate.now() : dataReferencia;
        return obterPorObra(obraId).identificarFasesAtrasadas(referencia).stream()
                .map(fase -> FaseDto.de(fase, referencia))
                .toList();
    }

    private Cronograma obterPorObra(Long obraId) {
        return cronogramas.findByObraId(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cronograma da obra", obraId));
    }
}
