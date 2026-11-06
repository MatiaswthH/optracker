export function formatFechaHora(fechaHoraActual: any) {
  // Si es un array tipo [2025, 1, 1, 0, 0]
  if (Array.isArray(fechaHoraActual) && fechaHoraActual.length >= 5) {
    const [year, month, day, hour, minute] = fechaHoraActual;
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${pad(year)}-${pad(month)}-${day} ${pad(hour)}:${pad(minute)}`;
  }
  // Si es objeto con year, month, day, hour, minute
  if (
    typeof fechaHoraActual === "object" &&
    fechaHoraActual !== null &&
    "year" in fechaHoraActual
  ) {
    const { year, month, day, hour, minute } = fechaHoraActual;
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${pad(hour)}:${pad(minute)} ${pad(day)}/${pad(month)}/${year}`;
  }
  // Si es string ISO
  if (typeof fechaHoraActual === "string") {
    const date = new Date(fechaHoraActual);
    if (!isNaN(date.getTime())) {
      const pad = (n: number) => n.toString().padStart(2, "0");
      return `${pad(date.getHours())}:${pad(date.getMinutes())} ${pad(
        date.getDate()
      )}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
    }
  }
  // Fallback
  return "";
}

export function formatFechaHoraVerbose(fechaHoraActual: any) {
  const monthNames = [
    "Ene",
    "Feb",
    "Mar",
    "Abr",
    "May",
    "Jun",
    "Jul",
    "Ago",
    "Sep",
    "Oct",
    "Nov",
    "Dic",
  ];
  // Si es un array tipo [2025, 1, 1, 14, 30]
  if (Array.isArray(fechaHoraActual) && fechaHoraActual.length >= 5) {
    const [year, month, day, hour, minute] = fechaHoraActual;
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${pad(day)} ${monthNames[month - 1]}, ${year} - ${pad(hour)}:${pad(
      minute
    )}`;
  }
  // Si es objeto con year, month, day, hour, minute
  if (
    typeof fechaHoraActual === "object" &&
    fechaHoraActual !== null &&
    "year" in fechaHoraActual
  ) {
    const { year, month, day, hour, minute } = fechaHoraActual;
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${monthNames[month - 1]} ${pad(day)}, ${year} - ${pad(hour)}:${pad(
      minute
    )}`;
  }
  // Si es string ISO
  if (typeof fechaHoraActual === "string") {
    const date = new Date(fechaHoraActual);
    if (!isNaN(date.getTime())) {
      const pad = (n: number) => n.toString().padStart(2, "0");
      return `${pad(date.getDate())} ${
        monthNames[date.getMonth()]
      }, ${date.getFullYear()} - ${pad(date.getHours())}:${pad(
        date.getMinutes()
      )}`;
    }
  }
  // If it's a Date object
  if (fechaHoraActual instanceof Date && !isNaN(fechaHoraActual.getTime())) {
    return `${pad(fechaHoraActual.getDate())} ${
      monthNames[fechaHoraActual.getMonth()]
    }, ${fechaHoraActual.getFullYear()} - ${pad(
      fechaHoraActual.getHours()
    )}:${pad(fechaHoraActual.getMinutes())}`;
  }
  // Fallback
  return "";
}

export function formatDiferenciaHoras(duration: any) {
  // Asumiendo que duration es en segundos
  const pad = (n: number) => n.toString().padStart(2, "0");
  const totalMinutes = Math.floor(duration / 60);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${pad(hours)}h:${pad(minutes)}m`;
}

export function formatDiferenciaHorasMs(ms: number) {
  if (typeof ms !== "number" || isNaN(ms) || ms < 0) return "00h:00m";
  const totalMinutes = Math.floor(ms / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${pad(hours)}h:${pad(minutes)}m`;
}

export function arrayToDate(arr: number[]) {
  return new Date(arr[0], arr[1] - 1, arr[2], arr[3], arr[4]);
}

export function pad(n: number) {
  // Función para rellenar números con ceros a la izquierda
  return n.toString().padStart(2, "0");
}

export function horasMinutosSegundos(n: number) {
  const ss = Math.floor(n % 60);
  const mm = Math.floor(n / 60);
  const hh = Math.floor(n / 3600);
  return `${pad(hh)}:${pad(mm)}:${pad(ss)}`;
}
