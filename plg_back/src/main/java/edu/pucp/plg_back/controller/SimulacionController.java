package edu.pucp.plg_back.controller;

import edu.pucp.plg_back.simulation.SimulationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping(value = "/semanal")
    public SseEmitter returnSimulacionSemanal(@RequestParam String fechaInicio) {

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        // Cerrar el SSE para que no quedeje en el aire y de lag
        emitter.onCompletion(() -> {
            System.out.println("SSE connection completed.");
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            System.out.println("SSE connection timed out.");
        });
        emitter.onError((ex) -> {
            emitter.completeWithError(ex);
            System.out.println("SSE connection error: " + ex.getMessage());
        });

        new Thread(() -> {
            try {
                simulationService.iniciarSimulacion(fechaInicio, emitter);
                emitter.send(SseEmitter.event().name("finsimulacion").data("Fin Simulacion"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }).start();
        return emitter;
    }

}
