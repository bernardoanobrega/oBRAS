import Button from '../ui/Button.jsx';

export default function ErrorState({ mensagem, onTentarNovamente }) {
  return (
    <div className="rounded-xl border border-perigo/40 bg-perigo-fundo p-6 text-center">
      <p className="text-sm text-perigo">{mensagem}</p>
      {onTentarNovamente && (
        <Button variante="secundario" className="mt-3" onClick={onTentarNovamente}>
          Tentar novamente
        </Button>
      )}
    </div>
  );
}
