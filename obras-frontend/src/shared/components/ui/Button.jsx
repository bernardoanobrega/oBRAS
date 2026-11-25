const VARIANTES = {
  primario: 'bg-brand text-fundo hover:bg-brand-claro disabled:bg-brand-fundo disabled:text-texto-fraco',
  secundario: 'bg-superficie text-texto border border-borda-forte hover:bg-superficie-alta disabled:text-texto-fraco',
  perigo: 'bg-perigo-forte text-texto hover:bg-perigo disabled:bg-perigo-fundo disabled:text-texto-fraco',
  link: 'text-brand-claro hover:underline px-0',
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
