import { useEffect, useMemo, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import {
  Search,
  Upload,
  Folder,
  FolderOpen,
  FileText,
  Eye,
  Download,
  History,
  Plus,
  ShieldCheck,
  Building2,
} from "lucide-react";
import {
  listarObras,
  listarDocumentos,
  obterArquivo,
} from "../api/documentosApi.js";
import {
  CATEGORIAS,
  DISCIPLINAS,
  dataHora,
  tamanho,
  revisao,
  filtrarDocumentos,
} from "../components/documentosUtils.js";
import UploadDocumento from "../components/UploadDocumento.jsx";
import VincularPrestador from "../components/VincularPrestador.jsx";
import DocumentoModal from "../components/DocumentoModal.jsx";
import { mensagemErro } from "../../../shared/api/http.js";
import { usePermissao } from "../../../shared/auth/usePermissao.js";
import "../documentos.css";

const MODULO = "CONTRATOS_DOCUMENTOS";
export default function DocumentosPage() {
  const { obraId: param } = useParams();
  const [params, setParams] = useSearchParams();
  const obraId = param || params.get("obra") || "";
  const [obras, setObras] = useState([]);
  const [documentos, setDocumentos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [busca, setBusca] = useState("");
  const [categoria, setCategoria] = useState("");
  const [disciplina, setDisciplina] = useState("");
  const [somenteAtuais, setSomenteAtuais] = useState(false);
  const [upload, setUpload] = useState(null);
  const [vinculo, setVinculo] = useState(null);
  const [preview, setPreview] = useState(null);
  const [abrindo, setAbrindo] = useState(false);
  const [reload, setReload] = useState(0);
  const { pode } = usePermissao(obraId || null);
  const global = usePermissao();
  useEffect(() => {
    let vivo = true;
    listarObras()
      .then((o) => {
        if (vivo) setObras(o);
      })
      .catch((e) => {
        if (vivo) setErro(mensagemErro(e));
      });
    return () => {
      vivo = false;
    };
  }, [reload]);
  useEffect(() => {
    if (!obraId && obras.length)
      setParams({ obra: String(obras[0].id) }, { replace: true });
  }, [obraId, obras, setParams]);
  useEffect(() => {
    let vivo = true;
    setDocumentos([]);
    setErro("");
    setUpload(null);
    setVinculo(null);
    setPreview(null);
    if (!obraId) {
      setCarregando(false);
      return;
    }
    setCarregando(true);
    listarDocumentos(obraId)
      .then((d) => vivo && setDocumentos(d))
      .catch((e) => vivo && setErro(mensagemErro(e)))
      .finally(() => vivo && setCarregando(false));
    return () => {
      vivo = false;
    };
  }, [obraId, reload]);
  useEffect(
    () => () => {
      if (preview?.url) URL.revokeObjectURL(preview.url);
    },
    [preview],
  );
  const filtrados = useMemo(
    () => filtrarDocumentos(documentos, busca, categoria, disciplina),
    [documentos, busca, categoria, disciplina],
  );
  const anteriores = filtrados.reduce(
    (n, d) => n + Math.max(0, d.versoes.length - 1),
    0,
  );
  async function abrir(doc, versao, baixar = false) {
    setAbrindo(true);
    setErro("");
    try {
      const blob = await obterArquivo(obraId, doc.id, versao.numero);
      const url = URL.createObjectURL(blob);
      if (baixar) {
        const a = document.createElement("a");
        a.href = url;
        a.download = versao.nomeOriginal;
        document.body.appendChild(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      } else
        setPreview({
          url,
          versao,
          codigo: doc.codigo,
          atual: versao.numero === doc.versoes[0].numero,
        });
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setAbrindo(false);
    }
  }
  function selecionar(cat, disc = "") {
    setCategoria(cat);
    setDisciplina(disc);
  }
  return (
    <section className="documentos-area">
      <header className="doc-page-header">
        <div>
          <span className="doc-eyebrow">REPOSITÓRIO DA OBRA</span>
          <h1>Documentos</h1>
        </div>
        <div className="doc-toolbar">
          <Building2 size={16} />
          <label className="sr-only" htmlFor="obra-documentos">
            Obra
          </label>
          {param ? (
            <span>
              {obras.find((o) => String(o.id) === param)?.nome ||
                `Obra #${param}`}
            </span>
          ) : (
            <select
              id="obra-documentos"
              value={obraId}
              onChange={(e) => {
                setParams({ obra: e.target.value });
                setSucesso("");
              }}
            >
              <option value="" disabled>
                Selecione uma obra
              </option>
              {obras.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.nome}
                </option>
              ))}
            </select>
          )}
          {global.pode(MODULO, "VISUALIZAR") && (
            <Link to="/prestadores">Prestadores ↗</Link>
          )}
        </div>
      </header>
      <div className="doc-topbar">
        <div className="doc-search">
          <Search size={16} />
          <input
            aria-label="Buscar documento por código ou nome"
            placeholder="Buscar por código ou nome"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
        {obraId && pode(MODULO, "CRIAR") && (
          <button className="doc-primary" onClick={() => setUpload({})}>
            <Upload size={16} />
            Upload de documento
          </button>
        )}
      </div>
      {erro && (
        <div className="doc-error" role="alert">
          {erro}{" "}
          <button onClick={() => setReload((n) => n + 1)}>
            Tentar novamente
          </button>
        </div>
      )}
      {sucesso && (
        <p className="doc-success" role="status">
          {sucesso}
        </p>
      )}
      <div className="doc-grid">
        <aside className="doc-directories">
          <header>
            <strong>Diretórios da obra</strong>
            <span>{documentos.length} itens</span>
          </header>
          <button
            className={`doc-folder ${!categoria ? "selected" : ""}`}
            onClick={() => selecionar("")}
          >
            <FolderOpen size={16} />
            Todos os documentos<span>{documentos.length}</span>
          </button>
          {Object.entries(CATEGORIAS).map(([cat, label]) => (
            <div key={cat}>
              <button
                className={`doc-folder ${categoria === cat && !disciplina ? "selected" : ""}`}
                onClick={() => selecionar(cat)}
              >
                <Folder size={16} />
                {label}
                <span>
                  {documentos.filter((d) => d.categoria === cat).length}
                </span>
              </button>
              {["PROJETO_EXECUTIVO", "ESPECIFICACAO_TECNICA"].includes(cat) &&
                categoria === cat &&
                Object.entries(DISCIPLINAS).map(([disc, label]) => (
                  <button
                    key={disc}
                    onClick={() => selecionar(cat, disc)}
                    className={`doc-folder doc-subfolder ${disciplina === disc ? "selected" : ""}`}
                  >
                    <Folder size={14} />
                    {label}
                    <span>
                      {
                        documentos.filter(
                          (d) => d.categoria === cat && d.disciplina === disc,
                        ).length
                      }
                    </span>
                  </button>
                ))}
            </div>
          ))}
          <footer>
            <ShieldCheck size={15} />
            <span>Arquivos e revisões preservados</span>
          </footer>
        </aside>
        <div className="doc-list">
          <header className="doc-list-header">
            <div>
              <h2>
                {CATEGORIAS[categoria] || "Todos os documentos"}
                {disciplina ? ` / ${DISCIPLINAS[disciplina]}` : ""}
              </h2>
              <p>
                Projetos, contratos e especificações · Histórico de revisões
              </p>
            </div>
            <label className="doc-check">
              <input
                type="checkbox"
                checked={somenteAtuais}
                onChange={(e) => setSomenteAtuais(e.target.checked)}
              />
              Apenas atuais
            </label>
          </header>
          <div className="doc-stats">
            <span>
              <b>{filtrados.length}</b> documentos atuais
            </span>
            <span>
              <b>{anteriores}</b> revisões anteriores
            </span>
          </div>
          {carregando ? (
            <p className="doc-empty" role="status">
              Carregando documentos…
            </p>
          ) : !obraId ? (
            <div className="doc-empty">
              <Building2 />
              <h3>Nenhuma obra disponível</h3>
              <p>
                As obras com permissão de consulta aparecerão aqui após o
                cadastro no módulo de obras.
              </p>
            </div>
          ) : !filtrados.length ? (
            <div className="doc-empty">
              <FolderOpen />
              <h3>
                {busca || categoria
                  ? "Nenhum documento encontrado"
                  : "O repositório está pronto para receber arquivos"}
              </h3>
              <p>
                {busca || categoria
                  ? "Ajuste a busca ou selecione outro diretório."
                  : "Adicione contratos, licenças e projetos desta obra."}
              </p>
            </div>
          ) : (
            <div className="doc-cards">
              {filtrados.map((doc) => {
                const atual = doc.versoes[0];
                if (!atual) return null;
                return (
                  <article className="doc-card" key={doc.id}>
                    <div className="doc-card-main">
                      <div className="doc-file-icon">
                        <FileText size={20} />
                      </div>
                      <div className="doc-card-info">
                        <div className="doc-title-row">
                          <h3>{doc.codigo}</h3>
                          <span className="doc-badge">
                            ● {revisao(atual.numero)} · ATUAL
                          </span>
                        </div>
                        <p>{doc.titulo}</p>
                        <small>
                          Enviado por {atual.autorNome} em{" "}
                          {dataHora(atual.dataEnvio)} · {atual.formato} (
                          {tamanho(atual.tamanhoBytes)})
                        </small>
                      </div>
                      <div className="doc-actions">
                        {doc.categoria === "CONTRATO" &&
                          !doc.prestadorId &&
                          pode(MODULO, "EDITAR") && (
                            <button onClick={() => setVinculo(doc)}>
                              Vincular prestador
                            </button>
                          )}
                        <button
                          className="doc-primary"
                          disabled={abrindo}
                          onClick={() => abrir(doc, atual)}
                        >
                          <Eye size={14} />
                          Visualizar documento
                        </button>
                        <button
                          disabled={abrindo}
                          onClick={() => abrir(doc, atual, true)}
                        >
                          <Download size={14} />
                          Exportar
                        </button>
                        {pode(MODULO, "EDITAR") && (
                          <button
                            aria-label={`Nova revisão de ${doc.codigo}`}
                            title="Nova revisão"
                            onClick={() => setUpload(doc)}
                          >
                            <Plus size={16} />
                          </button>
                        )}
                      </div>
                    </div>
                    {!somenteAtuais && (
                      <details className="doc-history">
                        <summary>
                          <History size={13} />
                          Histórico de revisões{" "}
                          <span>{doc.versoes.length}</span>
                        </summary>
                        <div>
                          {doc.versoes.map((v) => (
                            <div
                              key={v.id}
                              className={`doc-version ${v.numero === atual.numero ? "current" : ""}`}
                            >
                              <span
                                className={`doc-badge ${v.numero !== atual.numero ? "old" : ""}`}
                              >
                                {revisao(v.numero)} ·{" "}
                                {v.numero === atual.numero
                                  ? "ATUAL"
                                  : "ANTERIOR"}
                              </span>
                              <time>{dataHora(v.dataEnvio)}</time>
                              <p>{v.descricao}</p>
                              <button
                                disabled={abrindo}
                                onClick={() => abrir(doc, v)}
                              >
                                Consultar
                              </button>
                            </div>
                          ))}
                        </div>
                      </details>
                    )}
                  </article>
                );
              })}
            </div>
          )}
        </div>
      </div>
      {upload && (
        <UploadDocumento
          obraId={obraId}
          documento={upload.id ? upload : null}
          fechar={() => setUpload(null)}
          salvo={() => {
            setUpload(null);
            setSucesso("Documento publicado com sucesso.");
            setReload((n) => n + 1);
          }}
        />
      )}
      {vinculo && (
        <VincularPrestador
          obraId={obraId}
          documento={vinculo}
          fechar={() => setVinculo(null)}
          salvo={() => {
            setVinculo(null);
            setSucesso("Prestador vinculado com sucesso.");
            setReload((n) => n + 1);
          }}
        />
      )}
      {preview && (
        <DocumentoModal
          amplo
          titulo={`${preview.codigo} · ${revisao(preview.versao.numero)}${preview.atual ? "" : " · VERSÃO ANTERIOR"}`}
          fechar={() => setPreview(null)}
        >
          <p className="doc-muted">{preview.versao.descricao}</p>
          {preview.versao.formato === "PDF" ? (
            <iframe
              title="Visualização do documento PDF"
              className="doc-preview"
              src={preview.url}
            />
          ) : (
            <img
              className="doc-preview"
              alt={preview.versao.nomeOriginal}
              src={preview.url}
            />
          )}
          <a
            className="doc-download-link"
            href={preview.url}
            download={preview.versao.nomeOriginal}
          >
            Baixar {preview.versao.nomeOriginal}
          </a>
        </DocumentoModal>
      )}
    </section>
  );
}
