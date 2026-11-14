package br.com.obras.obra.service;

import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.CurrentUser;
import br.com.obras.shared.security.ModuloSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObraService {
    private final ObraRepository obras;
    private final AlocacaoObraRepository alocacoes;
    private final AutorizacaoPort autorizacao;

    public ObraService(
        ObraRepository obras,
        AlocacaoObraRepository alocacoes,
        AutorizacaoPort autorizacao
    ) {
        this.obras = obras;
        this.alocacoes = alocacoes;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public List <ObraDto> listar() {
        Long usuarioId = CurrentUser.id();
        return obras.findAllByOrderByNomeAsc().stream()
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

    private Obra obter(Long obraId) {
        return obras.findById(obraId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Obra", obraId));
    }
}
