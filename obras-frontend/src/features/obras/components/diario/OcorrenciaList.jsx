import { useState } from 'react';
import Can from '../../../../shared/auth/Can.jsx';
import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import Button from '../../../../shared/components/ui/Button.jsx';
import { paraOcorrencia } from '../../api/adaptadoresDiario.js';
import { Badge } from '../detalhe/primitivos.jsx';
import { CARD } from '../detalhe/tokens.js';
import FotoUpload from './FotoUpload.jsx';

export default function OcorrenciaList({ obraId, ocorrencias = [], onEditar, onExcluir, onRecarregar }) {
  const [fotoPara, setFotoPara] = useState(null);
  const itens = (Array.isArray(ocorrencias) ? ocorrencias : []).map(paraOcorrencia).filter(Boolean);

  return (
    <section className={`${CARD} p-5`}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-texto">Registro de ocorrências e imprevistos</h3>
          <p className="text-xs text-texto-fraco">Registro do diário selecionado</p>
        </div>
      </header>

      {itens.length === 0 ? (
        <div className="mt-5">
          <EmptyState mensagem="Sem ocorrências registradas neste diário." />
        </div>
      ) : (
        <ol className="mt-5 space-y-4">
          {itens.map((item, indice) => (
            <li key={item.id ?? `${item.data}-${indice}`} className="flex flex-col gap-3 rounded-lg border border-borda p-3 md:flex-row md:items-start md:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-mono text-[11px] text-texto-fraco">{item.data}</span>
                  <Badge tom={item.tom ?? 'neutral'} dot>{item.tag}</Badge>
                </div>
                <p className="mt-1.5 break-words text-sm text-texto">{item.texto}</p>
                {item.paralisacao && (
                  <p className="mt-1 text-[11px] text-texto-fraco">
                    Paralisação: {item.paralisacao.inicio} → {item.paralisacao.fim}
                  </p>
                )}
                <p className="mt-1 text-[11px] text-texto-fraco">{item.fotos.length} foto(s)</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
                  <Button type="button" variante="secundario" onClick={() => setFotoPara(item.id)}>
                    Anexar foto
                  </Button>
                </Can>
                <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={obraId}>
                  <Button type="button" variante="secundario" onClick={() => onEditar?.(ocorrencias[indice])}>
                    Editar
                  </Button>
                </Can>
                <Can modulo="DETALHES_OBRA" acao="EXCLUIR" obraId={obraId}>
                  <Button type="button" variante="perigo" onClick={() => onExcluir?.(ocorrencias[indice])}>
                    Excluir
                  </Button>
                </Can>
              </div>
            </li>
          ))}
        </ol>
      )}

      <FotoUpload
        aberto={fotoPara != null}
        obraId={obraId}
        ocorrenciaId={fotoPara}
        onFechar={() => setFotoPara(null)}
        onSalvo={() => {
          setFotoPara(null);
          onRecarregar?.();
        }}
      />
    </section>
  );
}
