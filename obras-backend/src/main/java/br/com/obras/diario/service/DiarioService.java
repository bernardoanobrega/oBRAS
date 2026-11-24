package br.com.obras.diario.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.Paralisacao;
import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.diario.domain.RegistroFotografico;
import br.com.obras.diario.dto.AlterarDiarioDto;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.DiarioResumoDto;
import br.com.obras.diario.dto.FotoDownloadDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovaOcorrenciaRapidaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.dto.ParalisacaoDto;
import br.com.obras.diario.repository.OcorrenciaRepository;
import br.com.obras.diario.repository.ParalisacaoRepository;
import br.com.obras.diario.repository.RegistroDiarioRepository;
import br.com.obras.diario.repository.RegistroFotograficoRepository;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.CurrentUser;
import br.com.obras.shared.storage.ArquivoArmazenado;
import br.com.obras.shared.storage.ArquivoStorageService;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DiarioService {
    private static final Set<String> FORMATOS_FOTO = Set.of("PNG", "JPG");
    private static final String MSG_DUPLICADO = "Já existe registro para o dia";

    private final RegistroDiarioRepository diarios;
    private final OcorrenciaRepository ocorrencias;
    private final ParalisacaoRepository paralisacoes;
    private final RegistroFotograficoRepository fotos;
    private final ObraService obras;
    private final AuditoriaService auditoria;
    private final ArquivoStorageService storage;

    public DiarioService(
        RegistroDiarioRepository diarios,
        OcorrenciaRepository ocorrencias,
        ParalisacaoRepository paralisacoes,
        RegistroFotograficoRepository fotos,
        ObraService obras,
        AuditoriaService auditoria,
        ArquivoStorageService storage
    ) {
        this.diarios = diarios;
        this.ocorrencias = ocorrencias;
        this.paralisacoes = paralisacoes;
        this.fotos = fotos;
        this.obras = obras;
        this.auditoria = auditoria;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<DiarioResumoDto> listar(Long obraId) {
        obras.obterEntidade(obraId);
        return diarios.listarPorObra(obraId).stream()
            .map(d -> DiarioResumoDto.de(d, ocorrencias.contarPorDiario(d.getId())))
            .toList();
    }

    public DiarioDto criar(Long obraId, NovoDiarioDto dto) {
        RegistroDiario diario = criarDiario(
            obraId, dto.data(), dto.clima(), dto.descricao(), dto.equipePresenteIds()
        );
        return DiarioDto.de(diario, List.of());
    }

    @Transactional(readOnly = true)
    public DiarioDto buscar(Long obraId, Long diarioId) {
        return DiarioDto.de(obterDiario(obraId, diarioId), listarOcorrencias(diarioId));
    }

    public DiarioDto editar(Long obraId, Long diarioId, AlterarDiarioDto dto) {
        RegistroDiario diario = obterDiario(obraId, diarioId);
        diario.editar(dto.clima(), dto.descricao(), validarEquipe(obraId, dto.equipePresenteIds()));
        auditoria.registrar(diario.getAuditavel(), AcaoAuditoria.EDICAO, null);
        return DiarioDto.de(diarios.saveAndFlush(diario), listarOcorrencias(diarioId));
    }

    public void excluir(Long obraId, Long diarioId) {
        RegistroDiario diario = obterDiario(obraId, diarioId);
        auditoria.excluir(diario.getAuditavel());
        diario.excluirLogico();
        diarios.saveAndFlush(diario);
    }

    public OcorrenciaDto adicionarOcorrencia(Long obraId, Long diarioId, NovaOcorrenciaDto dto) {
        RegistroDiario diario = obterDiario(obraId, diarioId);
        return criarOcorrencia(
            diario, dto.categoria(), dto.descricao(), dto.dataHora(), dto.paralisacao()
        );
    }

    public OcorrenciaDto editarOcorrencia(Long obraId, Long ocorrenciaId, NovaOcorrenciaDto dto) {
        Ocorrencia ocorrencia = obterOcorrencia(obraId, ocorrenciaId);
        ocorrencia.editar(dto.dataHora(), dto.categoria(), dto.descricao());

        Paralisacao atual = ocorrencia.getParalisacao();
        if (dto.paralisacao() == null) {
            if (atual != null) {
                paralisacoes.delete(atual);
                ocorrencia.definirParalisacao(null);
            }
        } else if (atual == null) {
            Paralisacao criada = paralisacoes.saveAndFlush(Paralisacao.criar(
                ocorrencia, dto.paralisacao().inicio(), dto.paralisacao().fim()
            ));
            ocorrencia.definirParalisacao(criada);
        } else {
            atual.editar(dto.paralisacao().inicio(), dto.paralisacao().fim());
        }

        auditoria.registrar(ocorrencia.getAuditavel(), AcaoAuditoria.EDICAO, null);
        return OcorrenciaDto.de(ocorrencias.saveAndFlush(ocorrencia));
    }

    public void excluirOcorrencia(Long obraId, Long ocorrenciaId) {
        auditoria.excluir(obterOcorrencia(obraId, ocorrenciaId).getAuditavel());
    }

    public OcorrenciaDto ocorrenciaRapida(Long obraId, NovaOcorrenciaRapidaDto dto) {
        obras.obterEntidade(obraId);
        RegistroDiario diario = diarios.findAtivo(obraId, dto.data()).orElse(null);
        if (diario == null) {
            if (dto.clima() == null) {
                throw new RegraNegocioException("Clima é obrigatório quando não existe diário no dia");
            }
            diario = criarDiario(obraId, dto.data(), dto.clima(), dto.descricao(), Set.of());
        }
        CategoriaOcorrencia categoria = dto.categoria() == null
            ? CategoriaOcorrencia.OUTRO
            : dto.categoria();
        return criarOcorrencia(
            diario, categoria, dto.descricao(), dto.dataHora(), dto.paralisacao()
        );
    }

    public FotoDto anexarFoto(Long obraId, Long ocorrenciaId, MultipartFile arquivo, String legenda) {
        if (legenda != null && legenda.length() > 300) {
            throw new RegraNegocioException("Legenda excede 300 caracteres");
        }
        Ocorrencia ocorrencia = obterOcorrencia(obraId, ocorrenciaId);
        ArquivoArmazenado armazenado = storage.armazenar(arquivo);
        if (!FORMATOS_FOTO.contains(armazenado.formato())) {
            storage.descartar(armazenado.localizador());
            throw new RegraNegocioException("Fotos: use PNG ou JPG");
        }
        try {
            Auditavel auditavel = auditoria.criar(AcaoAuditoria.CRIACAO, null);
            RegistroFotografico foto = RegistroFotografico.criar(
                auditavel, ocorrencia, armazenado.localizador(), legenda
            );
            return FotoDto.de(fotos.saveAndFlush(foto));
        } catch (RuntimeException e) {
            storage.descartar(armazenado.localizador());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public FotoDownloadDto buscarFoto(Long obraId, Long fotoId) {
        RegistroFotografico foto = obterFoto(obraId, fotoId);
        Resource resource = storage.carregar(foto.getArquivo());
        return new FotoDownloadDto(resource, mediaType(foto.getArquivo()), nomeArquivo(foto.getArquivo()));
    }

    public void excluirFoto(Long obraId, Long fotoId) {
        auditoria.excluir(obterFoto(obraId, fotoId).getAuditavel());
    }

    private RegistroDiario criarDiario(
        Long obraId,
        LocalDate data,
        Clima clima,
        String descricao,
        Set<Long> equipeIds
    ) {
        Obra obra = obras.obterEntidade(obraId);
        if (diarios.findAtivo(obraId, data).isPresent()) {
            throw new RegraNegocioException(MSG_DUPLICADO);
        }
        List<AlocacaoObra> equipe = validarEquipe(obraId, equipeIds);
        Auditavel auditavel = auditoria.criar(AcaoAuditoria.CRIACAO, null);
        RegistroDiario diario = RegistroDiario.criar(auditavel, obra, data, clima, descricao, equipe);
        try {
            return diarios.saveAndFlush(diario);
        } catch (DataIntegrityViolationException e) {
            if (mensagemContem(e, "uk_registro_diario_obra_data_ativo")) {
                throw new RegraNegocioException(MSG_DUPLICADO);
            }
            throw e;
        }
    }

    private OcorrenciaDto criarOcorrencia(
        RegistroDiario diario,
        CategoriaOcorrencia categoria,
        String descricao,
        LocalDateTime dataHora,
        ParalisacaoDto paralisacao
    ) {
        Long obraId = diario.getObra().getId();
        AlocacaoObra autor = obterAutor(obraId);
        Auditavel auditavel = auditoria.criar(AcaoAuditoria.CRIACAO, null);
        Ocorrencia ocorrencia = Ocorrencia.criar(auditavel, diario, autor, dataHora, categoria, descricao);
        ocorrencia = ocorrencias.saveAndFlush(ocorrencia);
        if (paralisacao != null) {
            Paralisacao criada = paralisacoes.saveAndFlush(Paralisacao.criar(
                ocorrencia, paralisacao.inicio(), paralisacao.fim()
            ));
            ocorrencia.definirParalisacao(criada);
        }
        return OcorrenciaDto.de(ocorrencia);
    }

    private List<AlocacaoObra> validarEquipe(Long obraId, Set<Long> ids) {
        if (ids.isEmpty()) return List.of();
        List<AlocacaoObra> encontradas = obras.alocacoesAtivasPorIds(obraId, ids);
        if (encontradas.size() != ids.size()) {
            throw new RegraNegocioException("Equipe presente contém alocação inválida ou inativa");
        }
        return encontradas;
    }

    private AlocacaoObra obterAutor(Long obraId) {
        return obras.alocacaoAtiva(obraId, CurrentUser.id())
            .orElseThrow(() -> new RegraNegocioException(
                "Usuário autenticado não está alocado ativamente na obra"
            ));
    }

    private RegistroDiario obterDiario(Long obraId, Long diarioId) {
        return diarios.buscar(obraId, diarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("RegistroDiario", diarioId));
    }

    private Ocorrencia obterOcorrencia(Long obraId, Long ocorrenciaId) {
        return ocorrencias.buscar(obraId, ocorrenciaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia", ocorrenciaId));
    }

    private RegistroFotografico obterFoto(Long obraId, Long fotoId) {
        return fotos.buscar(obraId, fotoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("RegistroFotografico", fotoId));
    }

    private List<OcorrenciaDto> listarOcorrencias(Long diarioId) {
        return ocorrencias.listarPorDiario(diarioId).stream()
            .map(OcorrenciaDto::de)
            .toList();
    }

    private boolean mensagemContem(Throwable erro, String trecho) {
        Throwable atual = erro;
        while (atual != null) {
            if (atual.getMessage() != null && atual.getMessage().contains(trecho)) return true;
            atual = atual.getCause();
        }
        return false;
    }

    private String nomeArquivo(String localizador) {
        return localizador.substring(localizador.lastIndexOf('/') + 1);
    }

    private MediaType mediaType(String localizador) {
        String caminho = localizador.toLowerCase();
        if (caminho.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (caminho.endsWith(".jpg") || caminho.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
