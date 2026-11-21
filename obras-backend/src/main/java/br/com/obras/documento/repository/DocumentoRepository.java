package br.com.obras.documento.repository;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.storage.ArquivoArmazenado;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class DocumentoRepository {
    private final JdbcClient jdbc;
    public DocumentoRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public List<Prestador> prestadores() {
        return jdbc.sql("SELECT * FROM prestador_servico ORDER BY nome_razao_social").query(Prestador.class).list();
    }
    public Prestador prestador(Long id, boolean bloquear) {
        return jdbc.sql("SELECT * FROM prestador_servico WHERE id=:id" + (bloquear ? " FOR UPDATE" : ""))
                .param("id", id).query(Prestador.class).optional()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador", id));
    }
    public Long criarPrestador(String nome, String cpf, String contato) {
        return jdbc.sql("INSERT INTO prestador_servico(nome_razao_social,cpf_cnpj,contato) VALUES (:nome,:cpf,:contato) RETURNING id")
                .param("nome", nome).param("cpf", cpf).param("contato", contato).query(Long.class).single();
    }
    public void editarPrestador(Long id, String nome, String cpf, String contato) {
        jdbc.sql("UPDATE prestador_servico SET nome_razao_social=:nome,cpf_cnpj=:cpf,contato=:contato WHERE id=:id")
                .param("id", id).param("nome", nome).param("cpf", cpf).param("contato", contato).update();
    }
    public void desativar(Long id) {
        jdbc.sql("UPDATE prestador_servico SET situacao='INATIVO' WHERE id=:id").param("id", id).update();
    }
    public Long criar(Long obraId, String codigo, String titulo, String categoria, String disciplina, Long prestadorId) {
        return jdbc.sql("""
                INSERT INTO documento(obra_id,codigo,titulo,categoria,disciplina,prestador_id)
                VALUES (:obra,:codigo,:titulo,:categoria,:disciplina,:prestador) RETURNING id
                """).param("obra", obraId).param("codigo", codigo).param("titulo", titulo)
                .param("categoria", categoria).param("disciplina", disciplina).param("prestador", prestadorId)
                .query(Long.class).single();
    }
    public Documento documento(Long obraId, Long id, boolean bloquear) {
        // Confere a relação obra/documento também para versões, downloads e vínculos.
        return jdbc.sql("SELECT * FROM documento WHERE obra_id=:obra AND id=:id" + (bloquear ? " FOR UPDATE" : ""))
                .param("obra", obraId).param("id", id)
                .query((rs, n) -> new Documento(rs.getLong("id"), rs.getLong("obra_id"),
                        rs.getObject("prestador_id", Long.class), rs.getString("codigo"), rs.getString("titulo"),
                        rs.getString("categoria"), rs.getString("disciplina"),
                        rs.getTimestamp("data_cadastro").toLocalDateTime(), List.of()))
                .optional().orElseThrow(() -> new RecursoNaoEncontradoException("Documento", id));
    }
    public List<Long> ids(Long obraId) {
        return jdbc.sql("SELECT id FROM documento WHERE obra_id=:obra ORDER BY data_cadastro DESC,id DESC")
                .param("obra", obraId).query(Long.class).list();
    }
    public record Referencia(Long id, Long obraId) {}
    public List<Referencia> contratos(Long prestadorId) {
        return jdbc.sql("SELECT id,obra_id FROM documento WHERE prestador_id=:id ORDER BY data_cadastro DESC")
                .param("id", prestadorId).query(Referencia.class).list();
    }
    public List<Versao> versoes(Long id) {
        return jdbc.sql("""
                SELECT v.id,v.numero,v.descricao,v.arquivo_nome_original AS nome_original,
                       v.arquivo_formato AS formato,v.arquivo_tamanho_bytes AS tamanho_bytes,v.data_envio,
                       v.autor_envio_id AS autor_id,u.nome AS autor_nome
                  FROM versao_documento v JOIN usuario u ON u.id=v.autor_envio_id
                 WHERE v.documento_id=:id ORDER BY v.numero DESC
                """).param("id", id).query(Versao.class).list();
    }
    public Long adicionarVersao(Long id, int numero, String descricao, Long autor, ArquivoArmazenado arquivo) {
        return jdbc.sql("""
                INSERT INTO versao_documento(documento_id,numero,descricao,autor_envio_id,arquivo_nome_original,
                  arquivo_formato,arquivo_tamanho_bytes,arquivo_localizador)
                VALUES (:id,:numero,:descricao,:autor,:nome,:formato,:tamanho,:localizador) RETURNING id
                """).param("id", id).param("numero", numero).param("descricao", descricao).param("autor", autor)
                .param("nome", arquivo.nomeOriginal()).param("formato", arquivo.formato())
                .param("tamanho", arquivo.tamanhoBytes()).param("localizador", arquivo.localizador()).query(Long.class).single();
    }
    public ArquivoArmazenado arquivo(Long id, int numero) {
        return jdbc.sql("""
                SELECT arquivo_nome_original AS nome_original,arquivo_formato AS formato,
                       arquivo_tamanho_bytes AS tamanho_bytes,arquivo_localizador AS localizador
                  FROM versao_documento WHERE documento_id=:id AND numero=:numero
                """).param("id", id).param("numero", numero).query(ArquivoArmazenado.class).optional()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Versão", numero));
    }
    public void vincular(Long id, Long prestador) {
        jdbc.sql("UPDATE documento SET prestador_id=:prestador WHERE id=:id")
                .param("id", id).param("prestador", prestador).update();
    }
    public void evento(Long documento, Long versao, Long prestador, Long autor, String acao) {
        jdbc.sql("""
                INSERT INTO historico_documental_prestador(documento_id,versao_id,prestador_id,autor_id,acao)
                VALUES (:doc,:versao,:prestador,:autor,:acao)
                """).param("doc", documento).param("versao", versao).param("prestador", prestador)
                .param("autor", autor).param("acao", acao).update();
    }
    public List<Evento> eventos(Long prestador) {
        return jdbc.sql("""
                SELECT h.id,d.obra_id,h.documento_id,d.titulo,v.numero,h.acao,h.data_hora,u.nome AS autor_nome
                  FROM historico_documental_prestador h JOIN documento d ON d.id=h.documento_id
                  JOIN versao_documento v ON v.id=h.versao_id JOIN usuario u ON u.id=h.autor_id
                 WHERE h.prestador_id=:id ORDER BY h.data_hora DESC,h.id DESC
                """).param("id", prestador).query(Evento.class).list();
    }
}
