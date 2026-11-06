import { useEffect } from "react";
import stompClient from "../stomp-client"; // importa tu cliente global

export function useSimulacionDiaria(
  estado: number,
  onSnapshot: (snap: any) => void,
  onResultado: (resultado: any) => void,
  fechaInicio: string
) {

  useEffect(() => {
  if (estado === 1 || estado === 2) {
    const onConnect = () => {
      stompClient.subscribe("/topic/diaria", (message) => {
        const parsed = JSON.parse(message.body);
        if (parsed.tipo === "resultadoSimulacion") {
          onResultado(parsed);
        } else {
          onSnapshot(parsed);
        }
      });

      stompClient.publish({
        destination: "/app/simulation/start",
        body: fechaInicio,
      });
    };

    stompClient.onConnect = onConnect;
    if (!stompClient.active) stompClient.activate();
  }

  return () => {
    // Only deactivate if stopping
    if (estado === 0 && stompClient.active) {
      stompClient.deactivate();
    }
  };
}, [estado, fechaInicio, onSnapshot, onResultado]);
}
