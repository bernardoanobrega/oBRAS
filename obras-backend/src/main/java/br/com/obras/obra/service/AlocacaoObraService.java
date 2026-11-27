package br.com.obras.obra.service;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.obra.dto.NovaAlocacaoDto;
import br.com.obras.obra.mapper.AlocacaoObraMapper;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.CargoConsultaPort;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlocacaoObraService {
    private final ObraService obras;
    private final AlocacaoObraRepository alocacoes;
    private final UsuarioConsultaPort usuarios;
    private final CargoConsultaPort cargos;
    private final AlocacaoObraMapper mapper;

    public AlocacaoObraService(ObraService obras, AlocacaoObraRepository alocacoes,
                               UsuarioConsultaPort usuarios, CargoConsultaPort cargos,
                               AlocacaoObraMapper mapper) {
        this.obras = obras;
        this.alocacoes = alocacoes;
        this.usuarios = usuarios;
        this.cargos = cargos;
        this.mapper = mapper;
    }

    @Transactional
    public AlocacaoObraDto alocar(Long obraId, NovaAlocacaoDto dto) {
        Obra obra = obras.obterEntidade(obraId);
        Usuario usuario = usuarios.buscarEntidade(dto.usuarioId())
                .filter(Usuario::estaAtivo)
                .orElseThrow(() -> new RegraNegocioException("Usuário inexistente ou inativo"));

        if (alocacoes.findAtivaPorUsuario(obraId, dto.usuarioId()).isPresent()) {
            throw new RegraNegocioException("Usuário já possui alocação ativa nesta obra");
        }

        Set<Long> ids = new HashSet<>(dto.cargoIds());
        if (ids.isEmpty()) {
            throw new RegraNegocioException("A alocação exige ao menos um cargo");
        }
        List<Cargo> encontrados = cargos.buscarPorIds(ids);
        Set<Long> encontradosIds = encontrados.stream().map(Cargo::getId).collect(Collectors.toSet());
        if (!encontradosIds.equals(ids)) {
            throw new RecursoNaoEncontradoException("Cargo", ids.stream()
                    .filter(id -> !encontradosIds.contains(id)).toList());
        }

        AlocacaoObra nova = AlocacaoObra.criar(obra, usuario, LocalDate.now(), new HashSet<>(encontrados));
        return mapper.paraDto(alocacoes.saveAndFlush(nova));
    }

    @Transactional
    public void encerrar(Long obraId, Long alocacaoId) {
        obras.obterEntidade(obraId);
        AlocacaoObra alocacao = alocacoes.buscarAtiva(obraId, alocacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocação ativa da obra", alocacaoId));
        alocacao.encerrar(LocalDate.now());
        alocacoes.save(alocacao);
    }

    @Transactional
    public AlocacaoObraDto atualizarCargos(Long obraId, Long alocacaoId, Set<Long> cargoIds) {
        obras.obterEntidade(obraId);
        if (cargoIds == null || cargoIds.isEmpty()) {
            throw new RegraNegocioException("A alocação exige ao menos um cargo");
        }
        AlocacaoObra alocacao = alocacoes.buscarAtiva(obraId, alocacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocação ativa da obra", alocacaoId));
        List<Cargo> encontrados = cargos.buscarPorIds(cargoIds);
        Set<Long> encontradosIds = encontrados.stream().map(Cargo::getId).collect(Collectors.toSet());
        if (!encontradosIds.equals(cargoIds)) {
            throw new RecursoNaoEncontradoException("Cargo", cargoIds.stream()
                    .filter(id -> !encontradosIds.contains(id)).toList());
        }
        alocacao.substituirCargos(new HashSet<>(encontrados));
        return mapper.paraDto(alocacoes.save(alocacao));
    }
}
