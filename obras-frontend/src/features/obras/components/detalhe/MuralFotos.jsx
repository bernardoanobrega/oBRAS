import { CARD } from './tokens.js';

/** Mural com apenas fotos de ocorrências. Placeholders locais (gradiente), sem upload avulso e sem link morto. */
export default function MuralFotos({ fotos }) {
  return (
    <section className={`${CARD} p-5`}>
      <header>
        <h3 className="text-sm font-semibold text-white">Mural de evidências fotográficas</h3>
        <p className="text-xs text-zinc-500">{fotos.length} registros de ocorrências</p>
      </header>

      <figure className="relative mt-4 overflow-hidden rounded-md">
        <div className={`aspect-[1408/768] w-full bg-gradient-to-br ${fotos[0].gradiente}`} />
        <figcaption className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-3 text-xs text-white">
          {fotos[0].legenda}
        </figcaption>
      </figure>

      <div className="mt-4 grid grid-cols-2 gap-4">
        {fotos.slice(1).map((f) => (
          <figure key={f.id}>
            <div className={`aspect-[4/3] w-full rounded bg-gradient-to-br ${f.gradiente}`} />
            <figcaption className="mt-1.5 text-[11px] text-zinc-300">{f.legenda}</figcaption>
          </figure>
        ))}
      </div>
    </section>
  );
}
