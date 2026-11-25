import { useEffect, useState } from "react";
import { Upload } from "lucide-react";
import DocumentoModal from "./DocumentoModal.jsx";
import { CATEGORIAS, DISCIPLINAS } from "./documentosUtils.js";
import {
  criarDocumento,
  publicarVersao,
  prestadoresDaObra,
} from "../api/documentosApi.js";
import { validarArquivo } from "../../../shared/utils/arquivo.js";
import { mensagemErro } from "../../../shared/api/http.js";

export default function UploadDocumento({ obraId, documento, fechar, salvo }) {
  const [categoria, setCategoria] = useState("PROJETO_EXECUTIVO");
  const [prestadores, setPrestadores] = useState([]);
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  useEffect(() => {
    let vivo = true;
    if (categoria === "CONTRATO" && !documento)
      prestadoresDaObra(obraId)
        .then((p) => vivo && setPrestadores(p))
        .catch((e) => vivo && setErro(mensagemErro(e)));
    return () => {
      vivo = false;
    };
  }, [categoria, documento, obraId]);
  async function enviar(e) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const falha = validarArquivo(form.get("arquivo"));
    if (falha) {
      setErro(falha);
      return;
    }
    if (!form.get("prestadorId")) form.delete("prestadorId");
    setEnviando(true);
    setErro("");
    try {
      if (documento) await publicarVersao(obraId, documento.id, form);
      else await criarDocumento(obraId, form);
      salvo();
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setEnviando(false);
    }
  }
  return (
    <DocumentoModal
      titulo={
        documento ? `Nova revisão · ${documento.codigo}` : "Upload de documento"
      }
      fechar={() => !enviando && fechar()}
    >
      <form className="doc-form" onSubmit={enviar}>
        {!documento && (
          <>
            <label>
              Código
              <input
                name="codigo"
                required
                maxLength={60}
                placeholder="Ex.: EST-VIG-PAV03"
              />
            </label>
            <label>
              Título
              <input
                name="titulo"
                required
                maxLength={200}
                placeholder="Ex.: Formas e armaduras das vigas"
              />
            </label>
            <label>
              Categoria
              <select
                name="categoria"
                value={categoria}
                onChange={(e) => setCategoria(e.target.value)}
              >
                {Object.entries(CATEGORIAS).map(([v, l]) => (
                  <option key={v} value={v}>
                    {l}
                  </option>
                ))}
              </select>
            </label>
            {["PROJETO_EXECUTIVO", "ESPECIFICACAO_TECNICA"].includes(
              categoria,
            ) && (
              <label>
                Disciplina
                <select name="disciplina">
                  {Object.entries(DISCIPLINAS).map(([v, l]) => (
                    <option key={v} value={v}>
                      {l}
                    </option>
                  ))}
                </select>
              </label>
            )}
            {categoria === "CONTRATO" && (
              <label>
                Prestador (opcional)
                <select name="prestadorId">
                  <option value="">Sem vínculo</option>
                  {prestadores.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nomeRazaoSocial}
                    </option>
                  ))}
                </select>
              </label>
            )}
          </>
        )}
        <label>
          Descrição da revisão
          <textarea
            name="descricao"
            required
            maxLength={1000}
            rows={3}
            placeholder="Descreva o conteúdo ou as alterações desta revisão"
          />
        </label>
        <label className="doc-upload">
          <Upload size={24} />
          Selecione o arquivo
          <input
            name="arquivo"
            type="file"
            accept="application/pdf,image/png,image/jpeg"
            required
          />
          <small>PDF, PNG ou JPG · Até 10 MB</small>
        </label>
        {documento && (
          <p className="doc-muted">
            A nova revisão se tornará a atual. As anteriores serão preservadas.
          </p>
        )}
        {erro && (
          <p className="doc-error" role="alert">
            {erro}
          </p>
        )}
        <footer>
          <button type="button" onClick={fechar} disabled={enviando}>
            Cancelar
          </button>
          <button className="doc-primary" disabled={enviando}>
            {enviando
              ? "Enviando…"
              : documento
                ? "Publicar revisão"
                : "Enviar documento"}
          </button>
        </footer>
      </form>
    </DocumentoModal>
  );
}
