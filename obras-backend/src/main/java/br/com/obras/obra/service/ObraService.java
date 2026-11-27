package br.com.obras.obra.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.domain.RegistroAuditoria;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.dto.AtualizarObraDto;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.CurrentUser;
import br.com.obras.shared.security.ModuloSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ObraService {
    private final ObraRepository obras;
    private final AlocacaoObraRepository alocacoes;
    private final AutorizacaoPort autorizacao;
    private final ObraMapper mapper;
    private final AuditoriaService auditoria;
    private final UsuarioConsultaPort usuarios;

    public ObraService(
        ObraRepository obras,
        AlocacaoObraRepository alocacoes,
        AutorizacaoPort autorizacao,
        ObraMapper mapper,
        AuditoriaService auditoria,
        UsuarioConsultaPort usuarios
    ) {
        this.obras = obras;
        this.alocacoes = alocacoes;
        this.autorizacao = autorizacao;
        this.mapper = mapper;
        this.auditoria = auditoria;
        this.usuarios = usuarios;
    }

    @Transactional
    public ObraDto criar(NovaObraDto dto) {
        validarResponsavelAtivo(dto.responsavelId());
        Auditavel auditavel = auditoria.criar(AcaoAuditoria.CRIACAO, null);
        Obra obra = obras.save(mapper.paraEntidade(dto, auditavel));
        return mapper.paraDto(obra);
    }

    @Transactional
    public ObraDto atualizar(Long obraId, AtualizarObraDto dto) {
        if (dto.responsavelId() != null) {
            throw new RegraNegocioException("Responsável muda por definirResponsavel");
        }
        Obra obra = obterEntidade(obraId);
        RegistroAuditoria registro = obra.atualizarParametros(
                dto.nome(),
                dto.descricao(),
                dto.tipologia(),
                dto.endereco(),
                dto.inicioPrevisto(),
                dto.fimPrevisto(),
                dto.art(),
                CurrentUser.id()
        );
        obras.save(obra);
        auditoria.registrar(registro);
        return mapper.paraDto(obra);
    }

    @Transactional
    public ObraDto definirResponsavel(Long obraId, Long responsavelId) {
        validarResponsavelAtivo(responsavelId);
        Obra obra = obterEntidade(obraId);
        RegistroAuditoria registro = obra.definirResponsavel(responsavelId, CurrentUser.id());
        obras.save(obra);
        auditoria.registrar(registro);
        return mapper.paraDto(obra);
    }

    @Transactional(readOnly = true)
    public List<ObraDto> listar() {
        Long usuarioId = CurrentUser.id();
        return obras.listarAtivas().stream()
            .filter(o -> autorizacao.possuiPermissao(
                usuarioId,
                o.getId(),
                ModuloSistema.DETALHES_OBRA,
                AcaoPermissao.VISUALIZAR
            ))
            .map(ObraDto::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public ObraDto buscar(Long obraId) {
        return ObraDto.de(obter(obraId));
    }

    @Transactional(readOnly = true)
    public List<EquipeMembroDto> equipe(Long obraId) {
        obter(obraId);
        return alocacoes.findEquipeAtiva(obraId).stream()
            .map(EquipeMembroDto::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public Obra obterEntidade(Long obraId) {
        return obter(obraId);
    }

    @Transactional(readOnly = true)
    public Optional<AlocacaoObra> alocacaoAtiva(Long obraId, Long usuarioId) {
        obter(obraId);
        return alocacoes.findAtivaPorUsuario(obraId, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<AlocacaoObra> alocacoesAtivasPorIds(Long obraId, Set<Long> ids) {
        obter(obraId);
        if (ids.isEmpty()) return List.of();
        return alocacoes.findAtivasPorIds(obraId, ids);
    }

    private void validarResponsavelAtivo(Long responsavelId) {
        if (responsavelId != null && !usuarios.estaAtivo(responsavelId)) {
            throw new RegraNegocioException("Responsável deve ser um usuário ativo");
        }
    }

    private Obra obter(Long obraId) {
        return obras.buscarAtiva(obraId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Obra", obraId));
    }
}
