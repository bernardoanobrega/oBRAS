export default function TextArea({ label, erro, id, className = '', rows = 3, ...props }) {
  const campo = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-texto">
          {label}
        </label>
      )}
      <textarea
        id={campo}
        rows={rows}
        className={`w-full rounded-lg border bg-fundo px-3 py-2 text-sm text-texto outline-none placeholder:text-texto-fraco focus:ring-2 focus:ring-brand ${
          erro ? 'border-perigo' : 'border-borda-forte'
        }`}
        {...props}
      />
      {erro && <p className="mt-1 text-xs text-perigo">{erro}</p>}
    </div>
  );
}
