export default function Card({ titulo, acoes, className = '', children }) {
  return (
    <section className={`rounded-xl border border-borda bg-superficie p-4 shadow-sm ${className}`}>
      {(titulo || acoes) && (
        <header className="mb-3 flex items-center justify-between gap-2">
          {titulo && <h2 className="font-semibold text-texto">{titulo}</h2>}
          {acoes}
        </header>
      )}
      {children}
    </section>
  );
}
