import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Search, Users, History, Pencil, UserX } from "lucide-react";
import {
  listarPrestadores,
  salvarPrestador,
  desativarPrestador,
  historicoPrestador,
} from "../api/documentosApi.js";
import { dataHora, revisao } from "../components/documentosUtils.js";
import DocumentoModal from "../components/DocumentoModal.jsx";
import { usePermissao } from "../../../shared/auth/usePermissao.js";
import { mensagemErro } from "../../../shared/api/http.js";
import "../documentos.css";

const MODULO = "CONTRATOS_DOCUMENTOS";
export default function PrestadoresPage() {
  const { pode } = usePermissao();
  const [lista, setLista] = useState([]);
  const [busca, setBusca] = useState("");
  const [erro, setErro] = useState("");
  const [erroModal, setErroModal] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [editar, setEditar] = useState(null);
  const [desativar, setDesativar] = useState(null);
  const [historico, setHistorico] = useState(null);
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let vivo = true;
    setCarregando(true);
    setErro("");
    listarPrestadores()
      .then((p) => vivo && setLista(p))
      .catch((e) => vivo && setErro(mensagemErro(e)))
      .finally(() => vivo && setCarregando(false));
    return () => {
      vivo = false;
    };
  }, [reload]);
  async function salvar(e) {
    e.preventDefault();
    setSalvando(true);
    setErroModal("");
    const dados = Object.fromEntries(new FormData(e.currentTarget));
    try {
      await salvarPrestador(editar.id, dados);
      setEditar(null);
      setReload((n) => n + 1);
    } catch (e) {
      setErroModal(mensagemErro(e));
    } finally {
      setSalvando(false);
    }
  }
  async function confirmarDesativacao() {
    setSalvando(true);
    setErroModal("");
    try {
      await desativarPrestador(desativar.id);
      setDesativar(null);
      setReload((n) => n + 1);
    } catch (e) {
      setErroModal(mensagemErro(e));
    } finally {
      setSalvando(false);
    }
  }
  async function abrirHistorico(p) {
    setErro("");
    try {
      setHistorico({ prestador: p, ...(await historicoPrestador(p.id)) });
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }
  const filtrados = lista.filter((p) =>
    `${p.nomeRazaoSocial} ${p.cpfCnpj}`
      .toLowerCase()
      .includes(busca.toLowerCase().trim()),
  );
  return (
    <section className="documentos-area">
      <header className="doc-page-header">
        <div>
          <span className="doc-eyebrow">CONTRATOS E DOCUMENTOS</span>
          <h1>Prestadores de serviço</h1>
        </div>
        <Link to="/documentos">Documentos ↗</Link>
      </header>
      <div className="doc-topbar">
        <div className="doc-search">
          <Search size={16} />
          <input
            aria-label="Buscar prestador"
            placeholder="Buscar por nome, CPF ou CNPJ"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
        {pode(MODULO, "CRIAR") && (
          <button
            className="doc-primary"
            onClick={() => {
              setEditar({});
              setErroModal("");
            }}
          >
            <Plus size={16} />
            Novo prestador
          </button>
        )}
      </div>
      {erro && (
        <div className="doc-error" role="alert">
          {erro}
          <button onClick={() => setReload((n) => n + 1)}>
            Tentar novamente
          </button>
        </div>
      )}
      <div className="doc-list">
        <header className="doc-list-header">
          <h2>Cadastro centralizado</h2>
          <span className="doc-muted">{filtrados.length} prestadores</span>
        </header>
        {carregando ? (
          <p className="doc-empty" role="status">
            Carregando prestadores…
          </p>
        ) : !filtrados.length ? (
          <div className="doc-empty">
            <Users />
            <h3>Nenhum prestador encontrado</h3>
            <p>
              Cadastre um prestador para vinculá-lo aos contratos das obras.
            </p>
          </div>
        ) : (
          <div className="doc-cards">
            {filtrados.map((p) => (
              <article className="doc-card doc-card-main" key={p.id}>
                <div className="doc-file-icon">
                  <Users size={20} />
                </div>
                <div className="doc-card-info">
                  <div className="doc-title-row">
                    <h3>{p.nomeRazaoSocial}</h3>
                    <span
                      className={`doc-badge ${p.situacao === "INATIVO" ? "old" : ""}`}
                    >
                      {p.situacao}
                    </span>
                  </div>
                  <p>{p.cpfCnpj}</p>
                  <small>{p.contato}</small>
                </div>
                <div className="doc-actions">
                  <button onClick={() => abrirHistorico(p)}>
                    <History size={14} />
                    Contratos e histórico
                  </button>
                  {pode(MODULO, "EDITAR") && (
                    <button
                      aria-label={`Editar ${p.nomeRazaoSocial}`}
                      onClick={() => {
                        setEditar(p);
                        setErroModal("");
                      }}
                    >
                      <Pencil size={14} />
                    </button>
                  )}
                  {p.situacao === "ATIVO" && pode(MODULO, "EXCLUIR") && (
                    <button
                      aria-label={`Desativar ${p.nomeRazaoSocial}`}
                      onClick={() => {
                        setDesativar(p);
                        setErroModal("");
                      }}
                    >
                      <UserX size={14} />
                    </button>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
      {editar && (
        <DocumentoModal
          titulo={editar.id ? "Editar prestador" : "Novo prestador"}
          fechar={() => !salvando && setEditar(null)}
        >
          <form className="doc-form" onSubmit={salvar}>
            <label>
              Nome ou razão social
              <input
                name="nomeRazaoSocial"
                required
                maxLength={200}
                defaultValue={editar.nomeRazaoSocial}
              />
            </label>
            <label>
              CPF ou CNPJ
              <input
                name="cpfCnpj"
                required
                maxLength={20}
                defaultValue={editar.cpfCnpj}
              />
            </label>
            <label>
              Contato
              <input
                name="contato"
                required
                maxLength={200}
                defaultValue={editar.contato}
                placeholder="Telefone ou e-mail"
              />
            </label>
            {erroModal && (
              <p className="doc-error" role="alert">
                {erroModal}
              </p>
            )}
            <footer>
              <button
                type="button"
                onClick={() => setEditar(null)}
                disabled={salvando}
              >
                Cancelar
              </button>
              <button className="doc-primary" disabled={salvando}>
                {salvando ? "Salvando…" : "Salvar prestador"}
              </button>
            </footer>
          </form>
        </DocumentoModal>
      )}
      {desativar && (
        <DocumentoModal
          titulo="Desativar prestador"
          fechar={() => !salvando && setDesativar(null)}
        >
          <p>
            Desativar {desativar.nomeRazaoSocial}? Seus contratos e histórico
            serão preservados. Novos vínculos ficarão bloqueados.
          </p>
          {erroModal && (
            <p className="doc-error" role="alert">
              {erroModal}
            </p>
          )}
          <div className="doc-actions doc-dialog-actions">
            <button disabled={salvando} onClick={() => setDesativar(null)}>
              Cancelar
            </button>
            <button
              className="doc-primary"
              disabled={salvando}
              onClick={confirmarDesativacao}
            >
              {salvando ? "Desativando…" : "Desativar"}
            </button>
          </div>
        </DocumentoModal>
      )}
      {historico && (
        <DocumentoModal
          amplo
          titulo={`Histórico · ${historico.prestador.nomeRazaoSocial}`}
          fechar={() => setHistorico(null)}
        >
          <h3>Contratos vinculados</h3>
          {historico.contratos.length ? (
            historico.contratos.map((c) => (
              <p className="doc-history-link" key={c.id}>
                <Link to={`/documentos?obra=${c.obraId}`}>
                  {c.codigo} · {c.titulo} ↗
                </Link>
              </p>
            ))
          ) : (
            <p className="doc-muted">
              Nenhum contrato disponível nas obras autorizadas.
            </p>
          )}
          <h3>Operações documentais</h3>
          {historico.eventos.length ? (
            historico.eventos.map((e) => (
              <div className="doc-version" key={e.id}>
                <span className="doc-badge old">{revisao(e.numero)}</span>
                <p>
                  {e.acao === "VINCULO_CONTRATO"
                    ? "Contrato vinculado"
                    : "Revisão publicada"}{" "}
                  · {e.titulo}
                  <small>
                    {e.autorNome} · {dataHora(e.dataHora)}
                  </small>
                </p>
              </div>
            ))
          ) : (
            <p className="doc-muted">Nenhum evento disponível.</p>
          )}
        </DocumentoModal>
      )}
    </section>
  );
}
