const VARIANTES = {
  primario: 'bg-brand-600 text-white hover:bg-brand-700 disabled:bg-brand-200',
  secundario: 'bg-white text-slate-700 border border-slate-300 hover:bg-slate-100 disabled:text-slate-400',
  perigo: 'bg-red-600 text-white hover:bg-red-700 disabled:bg-red-300',
  link: 'text-brand-700 hover:underline px-0',
};

// min-h 44px no mobile (toque), menor no desktop
export default function Button({ variante = 'primario', className = '', type = 'button', ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex min-h-[44px] items-center justify-center gap-2 rounded-lg px-4 text-sm font-medium transition md:min-h-[36px] disabled:cursor-not-allowed ${VARIANTES[variante]} ${className}`}
      {...props}
    />
  );
}
