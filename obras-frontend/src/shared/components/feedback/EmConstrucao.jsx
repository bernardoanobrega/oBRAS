import Card from '../ui/Card.jsx';

/** Placeholder de módulo ainda sem backend; mostra o escopo previsto na documentação. */
export default function EmConstrucao({ titulo, casosDeUso, requisitos, descricao }) {
  return (
    <div>
      <h1 className="mb-4 text-xl font-semibold">{titulo}</h1>
      <Card>
        <p className="text-sm text-slate-600">{descricao}</p>
        <dl className="mt-4 grid gap-2 text-sm md:grid-cols-2">
          <div>
            <dt className="font-medium">Casos de uso</dt>
            <dd className="text-slate-600">{casosDeUso}</dd>
          </div>
          <div>
            <dt className="font-medium">Requisitos</dt>
            <dd className="text-slate-600">{requisitos}</dd>
          </div>
        </dl>
        <p className="mt-4 rounded bg-amber-50 p-3 text-xs text-amber-800">
          Em construção: esta tela entra quando o módulo correspondente do backend estiver pronto.
        </p>
      </Card>
    </div>
  );
}
