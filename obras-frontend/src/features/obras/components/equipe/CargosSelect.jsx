/** Marca ou desmarca um cargo: devolve uma lista NOVA (não altera a original). */
export function alternarCargo(selecionados, cargoId) {
  return selecionados.includes(cargoId)
    ? selecionados.filter((id) => id !== cargoId)
    : [...selecionados, cargoId];
}

/** Seleção múltipla de cargos com caixas de marcar (a alocação pode ter vários cargos). */
export default function CargosSelect({ cargos, selecionados, onAlternar, erro }) {
  return (
    <fieldset>
      <legend className="mb-1 block text-xs font-medium text-texto-suave">Cargos *</legend>
      <div className={`grid gap-2 rounded-lg border p-3 sm:grid-cols-2 ${erro ? 'border-perigo' : 'border-borda'}`}>
        {cargos.map((c) => (
          <label key={c.id} className="flex min-h-[44px] cursor-pointer items-center gap-2 text-sm text-texto md:min-h-[32px]">
            <input
              type="checkbox"
              name="cargoIds"
              value={c.id}
              checked={selecionados.includes(c.id)}
              onChange={() => onAlternar?.(c.id)}
              className="size-4 accent-brand"
            />
            {c.nome}
          </label>
        ))}
      </div>
      {erro && <p className="mt-1 text-xs text-perigo">{erro}</p>}
    </fieldset>
  );
}
