package br.com.obras.shared.storage;

import br.com.obras.shared.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Valida (RNF-10: PDF/PNG/JPG, máx. 10 MB, conferindo a assinatura real do arquivo) e grava em disco.
 * Troque a implementação por S3/MinIO mantendo a mesma interface pública.
 */
@Service
public class ArquivoStorageService {
    public static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;

    private final Path base;

    public ArquivoStorageService(@Value("${app.storage.base-path}") String basePath) {
        this.base = Path.of(basePath).toAbsolutePath().normalize();
    }

    public ArquivoArmazenado armazenar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) throw new RegraNegocioException("Arquivo não informado");
        if (arquivo.getSize() > TAMANHO_MAXIMO) throw new RegraNegocioException("Arquivo excede o limite de 10 MB");

        String nome = arquivo.getOriginalFilename() == null
                ? "arquivo" : Path.of(arquivo.getOriginalFilename()).getFileName().toString();
        String formato = detectarFormato(arquivo);

        LocalDate hoje = LocalDate.now();
        String relativo = "%d/%02d/%s.%s".formatted(hoje.getYear(), hoje.getMonthValue(),
                UUID.randomUUID(), formato.toLowerCase());
        Path destino = base.resolve(relativo).normalize();
        try {
            Files.createDirectories(destino.getParent());
            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, destino);
            }
        } catch (IOException e) {
            throw new RegraNegocioException("Falha no envio do arquivo");
        }
        return new ArquivoArmazenado(nome, formato, arquivo.getSize(), relativo);
    }

    public Resource carregar(String localizador) {
        Path p = base.resolve(localizador).normalize();
        if (!p.startsWith(base) || !Files.exists(p)) throw new RegraNegocioException("Arquivo não encontrado");
        return new FileSystemResource(p);
    }

    public void descartar(String localizador) {
        try {
            Path p = base.resolve(localizador).normalize();
            if (p.startsWith(base)) Files.deleteIfExists(p);
        } catch (IOException ignored) { /* melhor esforço */ }
    }

    private String detectarFormato(MultipartFile f) {
        byte[] h = new byte[8];
        int lidos;
        try (InputStream in = f.getInputStream()) {
            lidos = in.read(h);
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo");
        }
        if (lidos >= 4 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F') return "PDF";
        if (lidos >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "PNG";
        if (lidos >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "JPG";
        throw new RegraNegocioException("Formato não permitido. Use PDF, PNG ou JPG");
    }
}
