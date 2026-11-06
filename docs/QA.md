**Pregunta 1:** ¿Cuál es el mapa de la ciudad?  
**Respuesta:** La ciudad es una retícula rectangular de 70 km de largo por 50 km de ancho. Todas las calles son de doble sentido, no hay diagonales ni curvas. Se modela como nodos en cada esquina con coordenadas (x,y), distancia 1 km entre nodos, origen en (0,0) esquina inferior izquierda.

**Pregunta 2:** ¿Dónde están los almacenes?  
**Respuesta:**  
- Almacén central: X=12, Y=8  
- Almacén intermedio Norte: X=42, Y=42  
- Almacén intermedio Este: X=63, Y=3

**Pregunta 3:** ¿Qué son los bloqueos?  
**Respuesta:** Son tramos de calle inhabilitados planificados, definidos por polígonos abiertos de nodos. Un nodo bloqueado no puede atravesarse ni girar. El archivo mensual “aaaamm.bloqueadas” lista intervalos (ddhmm-ddhmm) y secuencias de nodos bloqueados.

**Pregunta 4:** ¿Cuál es la flota de camiones cisternas?  
**Respuesta:**  
| Tipo | Tara (Ton) | Carga (m³) | Peso carga (Ton) | Peso combinado (Ton) | Unidades |
|------|------------|----------|----------|--------------|----------|
| TA   | 2.5        | 25       | 12.5     | 15.0         | 02       |
| TB   | 2.0        | 15       | 7.5      | 9.5          | 04       |
| TC   | 1.5        | 10       | 5.0      | 6.5          | 04       |
| TD   | 1.0        | 5        | 2.5      | 3.5          | 10       |

Consumo (galones) = Distancia (km) × Peso (ton) ÷ 180; velocidad promedio 50 km/h.

**Pregunta 5:** ¿Cuál es el plan de mantenimiento?  
**Respuesta:** Archivo “mantpreventivo” con registros aaaammdd:TTNN. Mantenimiento preventivo bimensual (cada 2 meses). Ejemplo para abril–mayo 2025:

20250401:TA01 20250403:TD01 ... 20250525:TD10

**Pregunta 6:** ¿Cuál es la capacidad de la flota de camiones cisterna? ¿Puede cambiar en el tiempo?  
**Respuesta:**  
- TA: 2 × 25 m³ = 50 m³  
- TB: 4 × 15 m³ = 60 m³  
- TC: 4 × 10 m³ = 40 m³  
- TD: 10 × 5 m³ = 50 m³  
Total 200 m³. Puede modificarse cargando nuevos datos en la base de datos.

**Pregunta 7:** ¿Cómo es el archivo histórico de pedidos?  
**Respuesta:** Mensual “ventas2025mm.txt”. Registro: \`##d##h##m:posX,posY,c-idCliente,m3,hLímite\`. Ejemplo:

11d13h31m:45,43,c-167,9m3,36h

**Pregunta 8:** ¿Cómo se ingresa los datos de mantenimiento de la flota?  
**Respuesta:** Archivo de texto “mantpreventivo” cargado en la base de datos antes de la primera presentación; verificado vía herramienta de visualización de BD.

**Pregunta 9:** ¿Qué son las averías?  
**Respuesta:** Incidentes que inmovilizan la unidad un tiempo (2–4 h) y pueden implicar estancia en taller. Tipos TI1, TI2, TI3 con reglas de disponibilidad por turnos. Se simulan vía “averias.txt” y ocurren aleatoriamente entre 5 % y 35 % de la ruta.

**Pregunta 10:** ¿Cuál es el horario de trabajo?  
**Respuesta:** Operaciones 24×7, sin interrupciones.

**Pregunta 11:** ¿Hay tráficos y semáforos?  
**Respuesta:** No; velocidad constante y semáforos siempre en verde.

**Pregunta 12:** ¿Cuál es el tiempo de carga/descarga en la planta?  
**Respuesta:** Despreciable (0 min).

**Pregunta 13:** ¿Cuál es el tiempo de descarga en la entrega en los clientes?  
**Respuesta:** 15 min.

**Pregunta 14:** ¿Ese tiempo de descarga en los clientes está considerado dentro del plazo de entrega?  
**Respuesta:** No; el plazo mide hasta la llegada del camión, no incluye descarga.

**Pregunta 15:** ¿Cuánto es el tiempo que debe pasar un camión en la planta, antes de volver a salir?  
**Respuesta:** 15 min de mantenimiento de rutina.

**Pregunta 16:** ¿Las averías deben ocurrir sí o sí en el rango de 5 % y 35 %?  
**Respuesta:** Sí; garantiza que ocurran con carga. Luego impactan la siguiente generación de rutas.

**Pregunta 17:** ¿Qué datos se reciben por la interfaz?  
**Respuesta:**  
- Día a día: pedidos por teclado y archivo de pedidos (formato histórico).  
- Simulaciones: fecha/hora de inicio, cubre 168 h siguientes, usa data proyectada.  
Se consulta estado de camiones (mantenimiento, averiado, en ruta).

**Pregunta 18:** ¿Entrada de datos para parametrización del algoritmo?  
**Respuesta:** Parámetros definidos por el equipo: Sa, Sc, Ta, tiempo de simulación (≤ 50 min). El mínimo de 4 h de plazo es variable.

**Pregunta 19:** ¿Qué es la replanificación?  
**Respuesta:** Cambio de rutas ante nuevos pedidos críticos. Ejemplo: primer juego de datos fuerza toda la flota a punto lejano; luego, tras 1 h, nuevo conjunto de pedidos obliga a ajustar rutas.

**Pregunta 20–30:** (Sin contenido específico o repetidos “¿s?”)

**Pregunta 30 (detalle):** ¿…?  
**Respuesta:** La fecha y hora del primer juego de datos debe ajustarse según escenario (día a día o semanal).
