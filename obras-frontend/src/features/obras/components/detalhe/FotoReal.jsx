import { useEffect, useState } from 'react';
import { buscarFotoBlob } from '../../api/diarioApi.js';

export default function FotoReal({ obraId, foto, className = 'aspect-[4/3]' }) {
  const [url, setUrl] = useState(null);
  const [falhou, setFalhou] = useState(false);

  useEffect(() => {
    let vivo = true;
    let criada = null;
    if (!obraId || !foto?.id) return undefined;

    buscarFotoBlob(obraId, foto.id)
      .then((blob) => {
        if (!vivo || typeof URL.createObjectURL !== 'function') {
          if (vivo) setFalhou(true);
          return;
        }
        criada = URL.createObjectURL(blob);
        setUrl(criada);
      })
      .catch(() => {
        if (vivo) setFalhou(true);
      });

    return () => {
      vivo = false;
      if (criada && typeof URL.revokeObjectURL === 'function') URL.revokeObjectURL(criada);
    };
  }, [obraId, foto?.id]);

  return (
    <figure className="overflow-hidden rounded-md">
      <div className={`w-full bg-zinc-800 ${className}`}>
        {url ? (
          <img src={url} alt={foto.legenda} className="size-full object-cover" />
        ) : (
          <div className="grid size-full place-items-center text-xs text-zinc-500">
            {falhou ? 'Foto indisponível' : 'Carregando foto…'}
          </div>
        )}
      </div>
      <figcaption className="mt-1.5 text-[11px] text-zinc-300">{foto.legenda}</figcaption>
    </figure>
  );
}
