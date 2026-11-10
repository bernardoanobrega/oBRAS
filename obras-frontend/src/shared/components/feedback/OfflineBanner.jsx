import { useEffect, useState } from 'react';
import { WifiOff } from 'lucide-react';

/** O sistema não tem modo offline: avisa e orienta a registrar com sinal. */
export default function OfflineBanner() {
  const [online, setOnline] = useState(navigator.onLine);

  useEffect(() => {
    const on = () => setOnline(true);
    const off = () => setOnline(false);
    window.addEventListener('online', on);
    window.addEventListener('offline', off);
    return () => {
      window.removeEventListener('online', on);
      window.removeEventListener('offline', off);
    };
  }, []);

  if (online) return null;
  return (
    <div className="flex items-center justify-center gap-2 bg-red-600 px-3 py-2 text-center text-sm text-white" role="alert">
      <WifiOff size={16} />
      Sem conexão. O oBRAS exige internet: aguarde o sinal para salvar seus registros.
    </div>
  );
}
