import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="mx-auto mt-16 max-w-md text-center">
      <h1 className="text-xl font-semibold">Página não encontrada</h1>
      <Link to="/" className="mt-3 inline-block text-sm text-brand-700 hover:underline">Voltar ao início</Link>
    </div>
  );
}
