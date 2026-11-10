import Button from '../ui/Button.jsx';

export default function ErrorState({ mensagem, onTentarNovamente }) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
      <p className="text-sm text-red-800">{mensagem}</p>
      {onTentarNovamente && (
        <Button variante="secundario" className="mt-3" onClick={onTentarNovamente}>
          Tentar novamente
        </Button>
      )}
    </div>
  );
}
