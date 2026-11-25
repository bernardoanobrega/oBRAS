/** opcoes: { valor: rótulo }. Mesmo visual e regras de acessibilidade do Input. */
export default function Select({ label, erro, id, opcoes, className = '', ...props }) {
  const campo = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-texto">
          {label}
        </label>
      )}
      <select
        id={campo}
        className={`min-h-[44px] w-full rounded-lg border bg-fundo px-3 text-sm text-texto outline-none placeholder:text-texto-fraco focus:ring-2 focus:ring-brand md:min-h-[36px] ${
          erro ? 'border-perigo' : 'border-borda-forte'
        }`}
        {...props}
      >
        {Object.entries(opcoes).map(([valor, rotulo]) => (
          <option key={valor} value={valor}>
            {rotulo}
          </option>
        ))}
      </select>
      {erro && <p className="mt-1 text-xs text-perigo">{erro}</p>}
    </div>
  );
}
