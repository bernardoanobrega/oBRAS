export default function Spinner({ className = '' }) {
  return (
    <div className={`flex justify-center ${className}`} role="status" aria-label="Carregando">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-brand-600" />
    </div>
  );
}
