package br.com.obras.diario.controller;

import br.com.obras.diario.dto.AlterarDiarioDto;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.DiarioResumoDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovaOcorrenciaRapidaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.service.DiarioService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}")
public class DiarioController {
    private final DiarioService service;

    public DiarioController(DiarioService service) {
        this.service = service;
    }

    @GetMapping("/diarios")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public List<DiarioResumoDto> listar(@PathVariable @P("obraId") Long obraId) {
        return service.listar(obraId);
    }

    @PostMapping("/diarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public DiarioDto criar(
        @PathVariable @P("obraId") Long obraId,
        @Valid @RequestBody NovoDiarioDto dto
    ) {
        return service.criar(obraId, dto);
    }

    @GetMapping("/diarios/{diarioId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public DiarioDto buscar(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long diarioId
    ) {
        return service.buscar(obraId, diarioId);
    }

    @PutMapping("/diarios/{diarioId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public DiarioDto editar(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long diarioId,
        @Valid @RequestBody AlterarDiarioDto dto
    ) {
        return service.editar(obraId, diarioId, dto);
    }

    @DeleteMapping("/diarios/{diarioId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EXCLUIR')")
    public void excluir(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long diarioId
    ) {
        service.excluir(obraId, diarioId);
    }

    @PostMapping("/diarios/{diarioId}/ocorrencias")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public OcorrenciaDto adicionarOcorrencia(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long diarioId,
        @Valid @RequestBody NovaOcorrenciaDto dto
    ) {
        return service.adicionarOcorrencia(obraId, diarioId, dto);
    }

    @PutMapping("/ocorrencias/{ocorrenciaId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public OcorrenciaDto editarOcorrencia(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long ocorrenciaId,
        @Valid @RequestBody NovaOcorrenciaDto dto
    ) {
        return service.editarOcorrencia(obraId, ocorrenciaId, dto);
    }

    @DeleteMapping("/ocorrencias/{ocorrenciaId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EXCLUIR')")
    public void excluirOcorrencia(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long ocorrenciaId
    ) {
        service.excluirOcorrencia(obraId, ocorrenciaId);
    }

    @PostMapping("/ocorrencias-rapidas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public OcorrenciaDto ocorrenciaRapida(
        @PathVariable @P("obraId") Long obraId,
        @Valid @RequestBody NovaOcorrenciaRapidaDto dto
    ) {
        return service.ocorrenciaRapida(obraId, dto);
    }

    @PostMapping(
        value = "/ocorrencias/{ocorrenciaId}/fotos",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public FotoDto anexarFoto(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long ocorrenciaId,
        @RequestPart("arquivo") MultipartFile arquivo,
        @RequestPart(value = "legenda", required = false) String legenda
    ) {
        return service.anexarFoto(obraId, ocorrenciaId, arquivo, legenda);
    }

    @GetMapping("/fotos/{fotoId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public ResponseEntity<Resource> buscarFoto(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long fotoId
    ) {
        var download = service.buscarFoto(obraId, fotoId);
        return ResponseEntity.ok()
            .contentType(download.mediaType())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.nomeArquivo() + "\"")
            .body(download.resource());
    }

    @DeleteMapping("/fotos/{fotoId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EXCLUIR')")
    public void excluirFoto(
        @PathVariable @P("obraId") Long obraId,
        @PathVariable Long fotoId
    ) {
        service.excluirFoto(obraId, fotoId);
    }
}
