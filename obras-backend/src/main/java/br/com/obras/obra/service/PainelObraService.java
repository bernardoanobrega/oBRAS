package br.com.obras.obra.service;

import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.DiarioResumoDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.service.DiarioService;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.obra.dto.PainelObraDto.ClimaDto;
import br.com.obras.obra.dto.PainelObraDto.KpisDto;
import br.com.obras.obra.dto.PainelObraDto.MaoDeObraDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PainelObraService {
    private final ObraService obras;
    private final DiarioService diarios;
    private final Clock clock;

    public PainelObraService(ObraService obras, DiarioService diarios, Clock clock) {
        this.obras = obras;
        this.diarios = diarios;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PainelObraDto painel(Long obraId) {
        ObraDto obra = obras.buscar(obraId);
        List<EquipeMembroDto> equipe = obras.equipe(obraId);
        LocalDate hoje = LocalDate.now(clock);
        List<DiarioResumoDto> resumos = diarios.listar(obraId);

        Optional<DiarioDto> diarioHoje = resumos.stream()
            .filter(r -> hoje.equals(r.data()))
            .findFirst()
            .map(r -> diarios.buscar(obraId, r.id()));

        if (diarioHoje.isEmpty()) {
            return new PainelObraDto(
                obra, ClimaDto.ausente(), KpisDto.simulados(), equipe,
                MaoDeObraDto.de(equipe.size(), 0, null),
                List.of(), List.of()
            );
        }

        DiarioDto diario = diarioHoje.get();
        int presentesHoje = tamanho(diario.equipePresente());
        Integer vsOntem = vsOntem(obraId, resumos, hoje, presentesHoje);

        List<OcorrenciaDto> ocorrencias = diario.ocorrencias() == null ? List.of() : diario.ocorrencias();
        List<FotoDto> fotos = ocorrencias.stream()
            .flatMap(o -> o.fotos() == null ? Stream.<FotoDto>empty() : o.fotos().stream())
            .toList();

        return new PainelObraDto(
            obra,
            new ClimaDto(diario.data(), diario.clima()),
            KpisDto.simulados(),
            equipe,
            MaoDeObraDto.de(equipe.size(), presentesHoje, vsOntem),
            ocorrencias,
            fotos
        );
    }

    /** Diário ativo imediatamente anterior a hoje (não D-1 literal). */
    private Integer vsOntem(Long obraId, List<DiarioResumoDto> resumos, LocalDate hoje, int presentesHoje) {
        return resumos.stream()
            .filter(r -> r.data().isBefore(hoje))
            .max(Comparator.comparing(DiarioResumoDto::data))
            .map(r -> presentesHoje - tamanho(diarios.buscar(obraId, r.id()).equipePresente()))
            .orElse(null);
    }

    private static int tamanho(List<?> lista) {
        return lista == null ? 0 : lista.size();
    }
}