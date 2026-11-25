/** opcoes: { valor: rótulo }. Mesmo visual e regras de acessibilidade do Input. */
export default function Select({ label, erro, id, opcoes, className = '', ...props }) {
  const campo = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <select
        id={campo}
        className={`min-h-[44px] w-full rounded-lg border bg-white px-3 text-sm outline-none focus:ring-2 focus:ring-brand-500 md:min-h-[36px] ${
          erro ? 'border-red-500' : 'border-slate-300'
        }`}
        {...props}
      >
        {Object.entries(opcoes).map(([valor, rotulo]) => (
          <option key={valor} value={valor}>
            {rotulo}
          </option>
        ))}
      </select>
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}
