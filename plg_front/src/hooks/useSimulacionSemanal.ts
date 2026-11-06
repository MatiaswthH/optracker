import { useEffect, useRef } from "react";
import url from "../utils/constant";

export function useSimulacionSemanal(
  estado: number,
  onSnapshot: (snap: any) => void,
  onResultado: (resultado: any) => void,
  fechaInicio: string
) {
  const prevEstado = useRef<number>(estado);

  useEffect(() => {
    // Only start EventSource if transitioning from 0 to 1
    if (!(prevEstado.current === 0 && estado === 1)) {
      prevEstado.current = estado;
      return;
    }

    const eventSource = new EventSource(
      url + `/simulacion/semanal?fechaInicio=${encodeURIComponent(fechaInicio)}`
    );

    eventSource.onopen = () => {
      console.log(
        "La conexión entre el cliente y el servidor es exitosa ......"
      );
    };

    eventSource.addEventListener("semana", (event: MessageEvent) => {
      const parsed = JSON.parse(event.data);
      onSnapshot(parsed);
    });

    eventSource.addEventListener("resultadoSemanal", (event: MessageEvent) => {
      const parsed = JSON.parse(event.data);
      onResultado(parsed);
    });

    eventSource.addEventListener("finsimulacion", (event: MessageEvent) => {
      console.log("SSE cerrado. Fin de simulación en el backend.");
      eventSource.close();
    });

    eventSource.onerror = () => {
      console.log("Error in EventSource connection");
      eventSource.close();
    };

    prevEstado.current = estado;

    return () => eventSource.close();
  }, [estado, onSnapshot]);
}
