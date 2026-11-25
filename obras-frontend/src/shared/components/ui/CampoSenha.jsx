import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

/**
 * Campo de senha com botão de mostrar/ocultar. Mesmo visual do Input.
 * Só muda o que aparece na tela (type password/text); o valor enviado é o mesmo.
 * visivelInicial existe para teste.
 */
export default function CampoSenha({ label, erro, id, className = '', visivelInicial = false, ...props }) {
  const [visivel, setVisivel] = useState(visivelInicial);
  const campo = id || props.name;
  const Icone = visivel ? EyeOff : Eye;

  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          id={campo}
          className={`min-h-[44px] w-full rounded-lg border pl-3 pr-11 text-sm outline-none focus:ring-2 focus:ring-brand-500 md:min-h-[36px] ${
            erro ? 'border-red-500' : 'border-slate-300'
          }`}
          {...props}
          type={visivel ? 'text' : 'password'}
        />
        <button
          type="button"
          onClick={() => setVisivel((v) => !v)}
          aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
          aria-pressed={visivel}
          className="absolute inset-y-0 right-0 grid w-11 place-items-center rounded-r-lg text-slate-500 hover:text-slate-800"
        >
          <Icone size={18} />
        </button>
      </div>
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}
