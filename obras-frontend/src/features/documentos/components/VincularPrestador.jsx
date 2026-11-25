import { useEffect, useState } from "react";
import DocumentoModal from "./DocumentoModal.jsx";
import { prestadoresDaObra, vincularPrestador } from "../api/documentosApi.js";
import { mensagemErro } from "../../../shared/api/http.js";

export default function VincularPrestador({
  obraId,
  documento,
  fechar,
  salvo,
}) {
  const [prestadores, setPrestadores] = useState([]);
  const [erro, setErro] = useState("");
  const [salvando, setSalvando] = useState(false);
  useEffect(() => {
    let vivo = true;
    prestadoresDaObra(obraId)
      .then((p) => vivo && setPrestadores(p))
      .catch((e) => vivo && setErro(mensagemErro(e)));
    return () => {
      vivo = false;
    };
  }, [obraId]);
  async function enviar(e) {
    e.preventDefault();
    const id = Number(new FormData(e.currentTarget).get("prestadorId"));
    setSalvando(true);
    setErro("");
    try {
      await vincularPrestador(obraId, documento.id, id);
      salvo();
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setSalvando(false);
    }
  }
  return (
    <DocumentoModal
      titulo={`Vincular prestador · ${documento.codigo}`}
      fechar={() => !salvando && fechar()}
    >
      <form className="doc-form" onSubmit={enviar}>
        <label>
          Prestador ativo
          <select name="prestadorId" required defaultValue="">
            <option value="" disabled>
              Selecione um prestador
            </option>
            {prestadores.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nomeRazaoSocial}
              </option>
            ))}
          </select>
        </label>
        <p className="doc-muted">
          O vínculo será registrado no histórico deste contrato.
        </p>
        {erro && (
          <p role="alert" className="doc-error">
            {erro}
          </p>
        )}
        <footer>
          <button type="button" disabled={salvando} onClick={fechar}>
            Cancelar
          </button>
          <button
            disabled={salvando || !prestadores.length}
            className="doc-primary"
          >
            {salvando ? "Vinculando…" : "Vincular prestador"}
          </button>
        </footer>
      </form>
    </DocumentoModal>
  );
}
