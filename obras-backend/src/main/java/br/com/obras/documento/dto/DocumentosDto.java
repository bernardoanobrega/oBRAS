package br.com.obras.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/** Contratos da API: o localizador interno dos arquivos nunca é enviado ao cliente. */
public final class DocumentosDto {
    private DocumentosDto() {}
    public record Prestador(Long id, String nomeRazaoSocial, String cpfCnpj, String contato, String situacao) {}
    public record DadosPrestador(@NotBlank @Size(max=200) String nomeRazaoSocial,
            @NotBlank @Size(max=20) String cpfCnpj, @NotBlank @Size(max=200) String contato) {}
    public record Versao(Long id, int numero, String descricao, String nomeOriginal, String formato,
            long tamanhoBytes, LocalDateTime dataEnvio, Long autorId, String autorNome) {}
    public record Documento(Long id, Long obraId, Long prestadorId, String codigo, String titulo,
            String categoria, String disciplina, LocalDateTime dataCadastro, List<Versao> versoes) {}
    public record Evento(Long id, Long obraId, Long documentoId, String titulo, int numero,
            String acao, LocalDateTime dataHora, String autorNome) {}
    public record Historico(List<Documento> contratos, List<Evento> eventos) {}
    public record Vinculo(Long prestadorId) {}
}
