export default function TextArea({ label, erro, id, className = '', rows = 3, ...props }) {
  const campo = id || props.name;
  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <textarea
        id={campo}
        rows={rows}
        className={`w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-brand-500 ${
          erro ? 'border-red-500' : 'border-slate-300'
        }`}
        {...props}
      />
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}
