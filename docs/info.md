
### Descripción de la Situación
PLG es una empresa de distribución de Gas Licuado de Petróleo (GLP) en una ciudad de 70 × 50 km organizada en una retícula de calles horizontales y verticales (nodos cada 1 km).  
- Los clientes realizan pedidos con al menos 4 horas de anticipación.  
- PLG dispone de un tanque principal siempre lleno y dos tanques intermedios de 160 m³ que se reabastecen cada día a las 00:00 h.  
- La flota consta de camiones cisterna de distintas capacidades (5–25 m³) sujetos a mantenimiento preventivo bimensual (24 h) y correctivo.  
- El consumo de combustible se calcula como `galones = distancia [km] × peso combinado [ton] / 180`.  
- Actualmente la planificación de rutas es manual, poco óptima y causa demoras y desperdicio de combustible.  

### Solución Esperada
Desarrollar una aplicación en Java (backend) y Bun + React + TypeScript (frontend) que incluya:  
1. **Gestión de Pedidos**  
   - Registro, validación y edición de pedidos (fecha/hora, volumen, coordenadas, plazo mínimo).  
2. **Componente Planificador**  
   - Algoritmos metaheurísticos en Java para planificar y replanificar rutas óptimas (distancia y consumo mínimo), respetando horarios de pedido, mantenimientos y bloqueos de calles.  
   - Soporte a tres escenarios: operación día a día, simulación semanal (20–50 min de ejecución) y simulación hasta colapso.  
3. **Componente Visualizador**  
   - Mapa interactivo en tiempo real con la cuadrícula de la ciudad, mostrando ubicación de camiones, rutas planificadas, estados (mantenimiento, averías) y tanques intermedios.  
   - Paneles de métricas y logs de eventos para monitorear desempeño de la flota.  
