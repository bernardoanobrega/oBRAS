package br.com.obras.documento.service;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.documento.repository.DocumentoRepository;
import br.com.obras.shared.domain.ObraDocumentalPort;
import br.com.obras.shared.exception.*;
import br.com.obras.shared.security.*;
import br.com.obras.shared.storage.*;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DocumentoService {
    private static final Set<String> CATEGORIAS = Set.of("CONTRATO", "LICENCA", "ALVARA", "ESPECIFICACAO_TECNICA", "PROJETO_EXECUTIVO");
    private static final Set<String> DISCIPLINAS = Set.of("GERAL", "ARQUITETONICO", "ESTRUTURAL", "ELETRICO", "HIDROSSANITARIO");
    private final DocumentoRepository repo;
    private final ArquivoStorageService storage;
    private final AutorizacaoPort autorizacao;
    private final ObraDocumentalPort obras;
    public DocumentoService(DocumentoRepository repo, ArquivoStorageService storage,
                            AutorizacaoPort autorizacao, ObraDocumentalPort obras) {
        this.repo=repo; this.storage=storage; this.autorizacao=autorizacao; this.obras=obras;
    }
    private boolean permitido(Long obra, AcaoPermissao acao) {
        return autorizacao.possuiPermissao(CurrentUser.id(), obra, ModuloSistema.CONTRATOS_DOCUMENTOS, acao);
    }
    private void exigir(Long obra, AcaoPermissao acao) {
        if (!permitido(obra, acao)) throw new AccessDeniedException("Acesso não autorizado");
    }
    private void exigirObra(Long obra, AcaoPermissao acao) {
        exigir(obra, acao);
        if (!obras.existe(obra)) throw new RecursoNaoEncontradoException("Obra", obra);
    }
    public List<ObraDocumentalPort.ObraResumo> obras() {
        return obras.listar().stream().filter(o -> permitido(o.id(), AcaoPermissao.VISUALIZAR)).toList();
    }
    public List<Prestador> prestadores() {
        exigir(null, AcaoPermissao.VISUALIZAR);
        return repo.prestadores();
    }
    public List<Prestador> prestadoresParaVinculo(Long obra) {
        if (!permitido(obra, AcaoPermissao.CRIAR) && !permitido(obra, AcaoPermissao.EDITAR))
            throw new AccessDeniedException("Acesso não autorizado");
        if (!obras.existe(obra)) throw new RecursoNaoEncontradoException("Obra", obra);
        return repo.prestadores().stream().filter(p -> p.situacao().equals("ATIVO")).toList();
    }
    @Transactional
    public Prestador salvarPrestador(Long id, DadosPrestador dados) {
        exigir(null, id == null ? AcaoPermissao.CRIAR : AcaoPermissao.EDITAR);
        String nome = texto(dados.nomeRazaoSocial(), 200, "Nome");
        String contato = texto(dados.contato(), 200, "Contato");
        String cpf = dados.cpfCnpj() == null ? "" : dados.cpfCnpj().replaceAll("[.\\-/\\s]", "");
        if (!cpf.matches("(?:[0-9]{11}|[0-9]{14})")) throw new RegraNegocioException("Informe CPF com 11 ou CNPJ com 14 dígitos");
        if (id == null) id = repo.criarPrestador(nome, cpf, contato);
        else { repo.prestador(id, true); repo.editarPrestador(id, nome, cpf, contato); }
        return repo.prestador(id, false);
    }
    @Transactional
    public Prestador desativar(Long id) {
        exigir(null, AcaoPermissao.EXCLUIR);
        repo.prestador(id, true);
        repo.desativar(id);
        return repo.prestador(id, false);
    }
    public Historico historico(Long id) {
        exigir(null, AcaoPermissao.VISUALIZAR);
        repo.prestador(id, false);
        var contratos = repo.contratos(id).stream().filter(r -> permitido(r.obraId(), AcaoPermissao.VISUALIZAR))
                .map(r -> completo(repo.documento(r.obraId(), r.id(), false))).toList();
        var eventos = repo.eventos(id).stream().filter(e -> permitido(e.obraId(), AcaoPermissao.VISUALIZAR)).toList();
        return new Historico(contratos, eventos);
    }
    public List<Documento> listar(Long obra) {
        exigirObra(obra, AcaoPermissao.VISUALIZAR);
        return repo.ids(obra).stream().map(id -> completo(repo.documento(obra, id, false))).toList();
    }
    public Documento buscar(Long obra, Long id) {
        exigirObra(obra, AcaoPermissao.VISUALIZAR);
        return completo(repo.documento(obra, id, false));
    }
    private Documento completo(Documento d) {
        return new Documento(d.id(),d.obraId(),d.prestadorId(),d.codigo(),d.titulo(),d.categoria(),
                d.disciplina(),d.dataCadastro(),repo.versoes(d.id()));
    }
    @Transactional
    public Documento criar(Long obra, String codigo, String titulo, String categoria, String disciplina,
                           Long prestador, String descricao, MultipartFile arquivo) {
        exigirObra(obra, AcaoPermissao.CRIAR);
        codigo = texto(codigo,60,"Código").toUpperCase(Locale.ROOT);
        titulo = texto(titulo,200,"Título");
        if (!CATEGORIAS.contains(categoria == null ? "" : categoria)) throw new RegraNegocioException("Categoria inválida");
        if (!DISCIPLINAS.contains(disciplina == null ? "" : disciplina)) throw new RegraNegocioException("Disciplina inválida");
        if (!categoria.equals("PROJETO_EXECUTIVO") && !categoria.equals("ESPECIFICACAO_TECNICA") && !disciplina.equals("GERAL"))
            throw new RegraNegocioException("Disciplina só se aplica a projetos e especificações técnicas");
        if (prestador != null) validarPrestador(categoria, prestador);
        descricao = texto(descricao,1000,"Descrição da revisão");
        ArquivoArmazenado salvo = armazenar(arquivo);
        Long id = repo.criar(obra,codigo,titulo,categoria,disciplina,prestador);
        Long versao = repo.adicionarVersao(id,1,descricao,CurrentUser.id(),salvo);
        if (prestador != null) {
            repo.evento(id,versao,prestador,CurrentUser.id(),"VINCULO_CONTRATO");
            repo.evento(id,versao,prestador,CurrentUser.id(),"INCLUSAO_VERSAO");
        }
        return completo(repo.documento(obra,id,false));
    }
    @Transactional
    public Documento revisar(Long obra, Long id, String descricao, MultipartFile arquivo) {
        exigirObra(obra, AcaoPermissao.EDITAR);
        descricao = texto(descricao,1000,"Descrição da revisão");
        // O lock do documento serializa a numeração. UNIQUE(documento_id,numero) é a segunda garantia.
        Documento d = repo.documento(obra,id,true);
        int numero = repo.versoes(id).getFirst().numero() + 1;
        ArquivoArmazenado salvo = armazenar(arquivo);
        Long versao = repo.adicionarVersao(id,numero,descricao,CurrentUser.id(),salvo);
        if (d.prestadorId() != null) repo.evento(id,versao,d.prestadorId(),CurrentUser.id(),"INCLUSAO_VERSAO");
        return completo(d);
    }
    @Transactional
    public Documento vincular(Long obra, Long id, Long prestador) {
        exigirObra(obra, AcaoPermissao.EDITAR);
        Documento d = repo.documento(obra,id,true);
        if (prestador == null) throw new RegraNegocioException("Selecione um prestador");
        if (prestador.equals(d.prestadorId())) return completo(d);
        if (d.prestadorId() != null) throw new RegraNegocioException("Este contrato já possui um prestador vinculado");
        validarPrestador(d.categoria(),prestador);
        repo.vincular(id,prestador);
        repo.evento(id,repo.versoes(id).getFirst().id(),prestador,CurrentUser.id(),"VINCULO_CONTRATO");
        return completo(repo.documento(obra,id,false));
    }
    private void validarPrestador(String categoria, Long id) {
        if (!categoria.equals("CONTRATO")) throw new RegraNegocioException("Somente contratos podem ser vinculados a prestadores");
        if (!repo.prestador(id,true).situacao().equals("ATIVO")) throw new RegraNegocioException("Prestador inativo não permite novos vínculos");
    }
    public record Download(Resource conteudo, ArquivoArmazenado metadados) {}
    public Download arquivo(Long obra, Long id, int numero) {
        exigirObra(obra, AcaoPermissao.VISUALIZAR);
        repo.documento(obra,id,false);
        ArquivoArmazenado a = repo.arquivo(id,numero);
        return new Download(storage.carregar(a.localizador()),a);
    }
    private ArquivoArmazenado armazenar(MultipartFile arquivo) {
        ArquivoArmazenado salvo = storage.armazenar(arquivo);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) storage.descartar(salvo.localizador());
            }
        });
        return salvo;
    }
    private static String texto(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > limite)
            throw new RegraNegocioException(campo + " é obrigatório e deve ter até " + limite + " caracteres");
        return valor.strip();
    }
}
