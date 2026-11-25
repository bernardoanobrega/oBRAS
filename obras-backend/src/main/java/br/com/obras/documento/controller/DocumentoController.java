package br.com.obras.documento.controller;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.documento.service.DocumentoService;
import br.com.obras.shared.domain.ObraDocumentalPort;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {
    private final DocumentoService service;
    public DocumentoController(DocumentoService service) { this.service=service; }
    @GetMapping("/obras")
    public List<ObraDocumentalPort.ObraResumo> obras() { return service.obras(); }
    @GetMapping("/obras/{obraId}")
    public List<Documento> listar(@PathVariable Long obraId) { return service.listar(obraId); }
    @GetMapping("/obras/{obraId}/prestadores")
    public List<Prestador> prestadores(@PathVariable Long obraId) { return service.prestadoresParaVinculo(obraId); }
    @GetMapping("/obras/{obraId}/{id}")
    public Documento buscar(@PathVariable Long obraId, @PathVariable Long id) { return service.buscar(obraId,id); }
    @PostMapping(value="/obras/{obraId}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Documento criar(@PathVariable Long obraId, @RequestParam String codigo, @RequestParam String titulo,
            @RequestParam String categoria, @RequestParam(defaultValue="GERAL") String disciplina,
            @RequestParam(required=false) Long prestadorId, @RequestParam String descricao,
            @RequestParam MultipartFile arquivo) {
        return service.criar(obraId,codigo,titulo,categoria,disciplina,prestadorId,descricao,arquivo);
    }
    @PostMapping(value="/obras/{obraId}/{id}/versoes", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Documento revisar(@PathVariable Long obraId, @PathVariable Long id,
            @RequestParam String descricao, @RequestParam MultipartFile arquivo) {
        return service.revisar(obraId,id,descricao,arquivo);
    }
    @PutMapping("/obras/{obraId}/{id}/prestador")
    public Documento vincular(@PathVariable Long obraId, @PathVariable Long id, @RequestBody Vinculo dados) {
        return service.vincular(obraId,id,dados.prestadorId());
    }
    @GetMapping("/obras/{obraId}/{id}/versoes/{numero}/arquivo")
    public ResponseEntity<Resource> arquivo(@PathVariable Long obraId, @PathVariable Long id,
            @PathVariable int numero, @RequestParam(defaultValue="false") boolean download) {
        var resultado = service.arquivo(obraId,id,numero);
        var a = resultado.metadados();
        MediaType tipo = switch (a.formato()) {
            case "PDF" -> MediaType.APPLICATION_PDF;
            case "PNG" -> MediaType.IMAGE_PNG;
            default -> MediaType.IMAGE_JPEG;
        };
        var disposicao = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(a.nomeOriginal(),StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(tipo).contentLength(a.tamanhoBytes())
                .cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION,disposicao.toString()).body(resultado.conteudo());
    }
}
