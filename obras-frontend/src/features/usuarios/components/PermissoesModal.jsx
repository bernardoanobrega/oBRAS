import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import { ACOES, MODULOS } from '../../../shared/constants/modulos.js';
import { mensagemErro } from '../../../shared/api/http.js';
import { definirPermissao, listarPermissoesIndividuais } from '../api/usuariosApi.js';

/**
 * Matriz módulo x ação das permissões INDIVIDUAIS (RF-01, UC02).
 * Marcar = concedida:true; desmarcar = concedida:false (retira o que o cargo concederia).
 * As permissões vindas do cargo na obra não aparecem aqui.
 */
export default function PermissoesModal({ aberto, usuario, onFechar }) {
  const [marcadas, setMarcadas] = useState(null);
  const [erro, setErro] = useState(null);
  const [salvando, setSalvando] = useState(null);

  async function carregar() {
    setErro(null);
    setMarcadas(null);
    try {
      const lista = await listarPermissoesIndividuais(usuario.id);
      setMarcadas(new Set(lista.filter((p) => p.concedida).map((p) => `${p.modulo}:${p.acao}`)));
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  useEffect(() => {
    if (aberto && usuario) carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aberto, usuario]);

  async function alternar(modulo, acao) {
    const chave = `${modulo}:${acao}`;
    const concedida = !marcadas.has(chave);
    setSalvando(chave);
    try {
      await definirPermissao(usuario.id, { modulo, acao, concedida });
      setMarcadas((atual) => {
        const novo = new Set(atual);
        if (concedida) novo.add(chave); else novo.delete(chave);
        return novo;
      });
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setSalvando(null);
    }
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo={`Permissões individuais — ${usuario?.nome ?? ''}`} largura="max-w-3xl">
      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregar} />}
      {!erro && !marcadas && <Spinner />}
      {marcadas && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left">
                <th className="py-2 pr-4">Módulo</th>
                {Object.values(ACOES).map((a) => <th key={a} className="px-2 py-2 text-center">{a}</th>)}
              </tr>
            </thead>
            <tbody>
              {Object.entries(MODULOS).map(([modulo, rotulo]) => (
                <tr key={modulo} className="border-b last:border-0">
                  <td className="py-2 pr-4 font-medium">{rotulo}</td>
                  {Object.keys(ACOES).map((acao) => {
                    const chave = `${modulo}:${acao}`;
                    return (
                      <td key={acao} className="px-2 py-2 text-center">
                        <input
                          type="checkbox"
                          className="h-5 w-5 accent-brand-600"
                          checked={marcadas.has(chave)}
                          disabled={salvando === chave}
                          onChange={() => alternar(modulo, acao)}
                          aria-label={`${rotulo} — ${ACOES[acao]}`}
                        />
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
          <p className="mt-3 text-xs text-slate-500">
            As alterações são salvas na hora. Permissões por obra vêm dos cargos da alocação do usuário.
          </p>
        </div>
      )}
    </Modal>
  );
}
