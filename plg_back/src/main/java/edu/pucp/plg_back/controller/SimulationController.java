package edu.pucp.plg_back.controller;

import edu.pucp.plg_back.simulation.SimulationService;
import edu.pucp.plg_back.util.FileLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @PostMapping("/start")
    public SseEmitter startSimulation(@RequestParam("fechaInicio") String fechaInicio) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        CompletableFuture.runAsync(() -> {
            try {
                FileLogger.log("Starting simulation with fecha: " + fechaInicio);
                simulationService.iniciarSimulacion(fechaInicio, emitter);
                emitter.complete();
            } catch (Exception e) {
                FileLogger.log("Error during simulation: " + e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    // Ignore if emitter is already closed
                }
            }
        });

        return emitter;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getSimulationStatus() {
        return ResponseEntity.ok("Simulation service is ready");
    }
}
