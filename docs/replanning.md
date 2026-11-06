## Replanning System Explanation

The replanning system is designed with a two-tiered approach to handle both long-term strategic planning and short-term tactical adjustments.

**I. Flow of Data and Structure:**

1.  **Long-Term Planning (3-Day Horizon):**
    *   Initiated by scheduling a `PlanificacionTareas` (or `PlanificacionColapso`) instance.
    *   `PlanificadorTareasServicios` is responsible for scheduling these `Runnable` tasks based on a cron expression defined in a `ControlTarea` object. The `ControlTarea` also provides the planning window (start/end dates) and the map model.
    *   Once `PlanificacionTareas.run()` executes:
        *   It fetches all orders (`pedidoService.obtenerPedidos3días`), available vehicles (`vehiculoService.listaVehiculosDisponibles`), and blockages (`bloqueoService.getBloqueosFechas3dias`) for the upcoming 3-day period. The `dia` static variable, initialized from `controlTarea.getFechaInicio()`, helps segment this 3-day window.
        *   It enters a `do-while` loop that continues as long as there are unassigned orders (`requestListDesdoblado`).
        *   **Inside the loop (each iteration representing a planning segment):**
            *   Vehicles are associated with priority queues of orders, prioritized by a ratio of time-to-limit vs. estimated travel time.
            *   Orders that cannot be reached in time by any vehicle are flagged ("colapsado").
            *   `Knapsack.allocate()`: Orders are assigned to vehicles based on capacity and the prioritized queues.
            *   `GeneticAlgorithm.Genetic()`: For each vehicle with assigned orders, the genetic algorithm optimizes the delivery route.
            *   Vehicle states (current location, available time) and order statuses (attended) are updated.
            *   Planned routes are collected.
            *   The list of available vehicles is sorted by their next available time.
            *   The list of orders is filtered to remove attended ones.
        *   After the loop (all orders for the 3-day window are planned), the aggregated routes (`TodoTresDias`) are sent via an `SseEmitter` with the event name "3dias".
        *   A `counter` tracks progress. Every 2 increments, `dia` is incremented (advancing the planning day within the 3-day window).
        *   When `counter` reaches 6 (signifying the end of the 3-day planning cycle), an "STOP" event is sent, the emitter is completed, and `planificadorTareasServicios.eliminarPlanificadorTareas()` is called to remove this task from the scheduler.
    *   `PlanificacionColapso` follows a very similar flow but is likely intended for simulating or handling "logistic collapse" scenarios. It sends different SSE event names ("SIMULCOLAPSO", "CALAPSO") and has a different `counter` limit (8) for completion. If a collapse is detected (all vehicles cannot serve a request), it sends a "CALAPSO" event and terminates.

2.  **Short-Term/Continuous Replanning (2-Minute Interval):**
    *   Managed by `ScheduledTasks`.
    *   The `reportCurrentTime()` method is annotated with `@Scheduled(fixedRate = 120000)`, causing it to execute every 2 minutes.
    *   **Inside `reportCurrentTime()`:**
        *   It fetches currently unattended orders (`pedidoService.listaPedidosSinAtender()`).
        *   It uses a list of vehicles initialized during `@PostConstruct` (which also sets up the map and initial vehicle states).
        *   Similar to `PlanificacionTareas`, it enters a `do-while` loop:
            *   Assigns orders to vehicles using `Knapsack.allocate()`.
            *   Optimizes routes using `GeneticAlgorithm.Genetic()`.
            *   Updates vehicle and order statuses.
        *   Aggregated routes (`EntidadRutas`) for the immediate assignments are sent via its own `SseEmitter` (static `emi` variable) with the event name "RUTAS".
    *   This provides a reactive layer to handle new orders or changes that occur between the larger 3-day planning cycles.

**II. Class Implementation and Attributes:**

*   **`PlanificacionTareas` (and `PlanificacionColapso`)**
    *   **Purpose:** Implements the logic for long-term (3-day) planning. `PlanificacionColapso` is a specialized version for collapse scenarios.
    *   **Implements:** `Runnable` (to be executed by `TaskScheduler`).
    *   **Key Attributes:**
        *   `uuid` (String): Unique identifier for the scheduled task, used to remove it later.
        *   `planificadorTareasServicios` (PlanificadorTareasServicios): Service to manage its own lifecycle (e.g., un-scheduling).
        *   `controlTarea` (ControlTarea): Contains input parameters for the planning run (dates, map, etc.).
        *   `emitter` (SseEmitter): Used to send planning results/updates to the client.
        *   `counter` (int): Tracks the number of planning segments processed within the 3-day window.
        *   `dia` (static int): Represents the current day being planned within the 3-day cycle.
        *   `pedidoService`, `vehiculoService`, `bloqueoService`: Autowired services to fetch and update data.
        *   `listaVehiculos` (List<EntidadVehiculo>): List of available vehicles for the planning period.
        *   `requestListDesdoblado` (List<PedidoModel>): List of orders to be planned.
        *   `blockList` (List<NodoModel>): List of blocked nodes on the map.
    *   **Custom Functions:**
        *   `run()`: The core method containing the entire 3-day planning logic described above. It orchestrates data fetching, order assignment (Knapsack), route optimization (Genetic Algorithm), state updates, and result emission.

*   **`ScheduledTasks`**
    *   **Purpose:** Handles continuous, short-term replanning for immediate unattended orders.
    *   **Annotations:** `@Component`, `@Scheduled(fixedRate = 120000)` (to run every 2 minutes).
    *   **Key Attributes:**
        *   `pedidoService`, `vehiculoService`, `bloqueoService`: Autowired services.
        *   `emi` (static SseEmitter): Emitter for sending real-time route updates.
        *   `listaVehiculos` (List<EntidadVehiculo>): List of currently available vehicles.
        *   `requestListDesdoblado` (List<PedidoModel>): List of current unattended orders.
        *   `mapaModel` (MapaModel): The grid map.
    *   **Custom Functions:**
        *   `@PostConstruct postConstruct()`: Initializes vehicle list, blockages, map model, and initial vehicle states (start time, location, fuel, speed) once the bean is created.
        *   `reportCurrentTime()`: The scheduled method that executes every 2 minutes. It fetches unattended orders and performs allocation and routing for them, sending updates via `emi`.

*   **`PlanificadorTareasServicios`**
    *   **Purpose:** Manages the scheduling and lifecycle of long-running tasks like `PlanificacionTareas`.
    *   **Annotations:** `@Service`.
    *   **Key Attributes:**
        *   `taskScheduler` (TaskScheduler): Spring's scheduler for running tasks.
        *   `jobsMap` (Map<String, ScheduledFuture<?>>): Stores references to scheduled tasks by their `jobId`, allowing them to be cancelled.
    *   **Custom Functions:**
        *   `planificarTareas(String jobId, Runnable tasklet, String cronExpression)`: Schedules the given `Runnable` task (e.g., `PlanificacionTareas`) to run based on the `cronExpression`. Stores the `ScheduledFuture` in `jobsMap`.
        *   `eliminarPlanificadorTareas(String jobId)`: Cancels the scheduled task associated with `jobId` and removes it from `jobsMap`.

*   **`ControlTarea`**
    *   **Purpose:** A data object (POJO) used to pass configuration and parameters to a `PlanificacionTareas` instance when it's scheduled.
    *   **Key Attributes:**
        *   `controlCron` (String): The cron expression defining when the planning task should run.
        *   `fechaInicio`, `fechaFin` (Date): The start and end dates for the planning horizon.
        *   `mapaModel` (MapaModel): The map representation.
        *   `ID` (String): An identifier for this control task.
        *   Services (`pedidoService`, `vehiculoService`, `bloqueoService`): These are also present, suggesting `ControlTarea` might be populated with necessary service instances, though `PlanificacionTareas` also autowires its own. This could be for context or specific setup.

*   **`ScheduledThreeDays`**
    *   **Purpose:** Appears to be an attempt or placeholder for another scheduled task related to 3-day planning, but its core logic (`planificaciondias()`) is currently commented out.
    *   **Key Attributes:**
        *   `emi` (static SseEmitter): Similar to `ScheduledTasks`, suggesting it was intended to send SSE events.
    *   **Custom Functions:**
        *   `planificaciondias()`: Currently contains commented-out code that seems to call an execution algorithm and send results. Its role in the active system is unclear without the uncommented logic.

**Core Algorithms Used:**

*   **`Knapsack.allocate(...)`**: This custom function is responsible for assigning a list of prioritized orders (from a `PriorityQueue`) to a list of vehicles, considering vehicle capacities and potentially other constraints. It updates which orders are assigned (`auxRequest` seems to track this).
*   **`GeneticAlgorithm.Genetic(...)`**: This custom function takes a vehicle, its list of assigned orders (vertices), and the map model. It applies a genetic algorithm to find an optimized route for that vehicle to deliver its assigned orders.