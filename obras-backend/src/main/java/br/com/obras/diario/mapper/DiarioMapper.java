package br.com.obras.diario.mapper;

import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.Paralisacao;
import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.diario.domain.RegistroFotografico;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.DiarioResumoDto;
import br.com.obras.diario.dto.FotoDownloadDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.MembroDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.dto.ParalisacaoDto;
import br.com.obras.obra.domain.AlocacaoObra;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converte entidades do diário em DTOs de saída (ADR-16).
 *
 * Só converte: não consulta banco, não valida e não decide nada. Quem escolhe
 * o que entra (ex.: só fotos ativas) é o service, usando os repositories. O
 * caminho inverso (DTO → entidade) fica no service + fábricas do domínio,
 * porque criar uma entidade envolve regra de negócio.
 */
@Component
public class DiarioMapper {

    public DiarioResumoDto paraResumo(RegistroDiario d, long totalOcorrencias) {
        return new DiarioResumoDto(d.getId(), d.getData(), d.getClima().name(), d.getDescricao(), totalOcorrencias);
    }

    /**
     * Diário completo. {@code fotosAtivas} são as fotos do diário inteiro; o mapper
     * só as distribui entre as ocorrências.
     */
    public DiarioDto paraDto(RegistroDiario d, List<Ocorrencia> ocorrencias, List<RegistroFotografico> fotosAtivas) {
        Map<Long, List<RegistroFotografico>> fotosPorOcorrencia = fotosAtivas.stream()
            .collect(Collectors.groupingBy(f -> f.getOcorrencia().getId()));

        List<MembroDiarioDto> equipe = d.getEquipePresente().stream()
            .map(this::paraMembro)
            .sorted(Comparator.comparing(MembroDiarioDto::nome, String.CASE_INSENSITIVE_ORDER))
            .toList();

        List<OcorrenciaDto> ocorrenciasDto = ocorrencias.stream()
            .map(o -> paraDto(o, fotosPorOcorrencia.getOrDefault(o.getId(), List.of())))
            .toList();

        return new DiarioDto(
            d.getId(),
            d.getObra().getId(),
            d.getData(),
            d.getClima().name(),
            d.getDescricao(),
            equipe,
            ocorrenciasDto
        );
    }

    /** Ocorrência com as fotos já filtradas (ativas) pelo service. */
    public OcorrenciaDto paraDto(Ocorrencia o, List<RegistroFotografico> fotosAtivas) {
        return new OcorrenciaDto(
            o.getId(),
            o.getRegistroDiario().getId(),
            o.getCategoria().name(),
            o.getDescricao(),
            o.getDataHora(),
            paraMembro(o.getAlocacaoObra()),
            paraDto(o.getParalisacao()),
            fotosAtivas.stream().map(this::paraDto).toList()
        );
    }

    public FotoDto paraDto(RegistroFotografico f) {
        return new FotoDto(f.getId(), f.getOcorrencia().getId(), nomeArquivo(f.getArquivo()), f.getLegenda(), f.getDataEnvio());
    }

    public ParalisacaoDto paraDto(Paralisacao p) {
        return p == null ? null : new ParalisacaoDto(p.getInicio(), p.getFim());
    }

    public MembroDiarioDto paraMembro(AlocacaoObra a) {
        return new MembroDiarioDto(a.getId(), a.getUsuario().getId(), a.getUsuario().getNome());
    }

    public FotoDownloadDto paraDownload(RegistroFotografico f, Resource conteudo) {
        return new FotoDownloadDto(conteudo, mediaType(f.getArquivo()), nomeArquivo(f.getArquivo()));
    }

    private static String nomeArquivo(String localizador) {
        return localizador.substring(localizador.lastIndexOf('/') + 1);
    }

    private static MediaType mediaType(String localizador) {
        String caminho = localizador.toLowerCase();
        if (caminho.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (caminho.endsWith(".jpg") || caminho.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
