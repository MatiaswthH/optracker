package edu.pucp.plg_back.controller;

import edu.pucp.plg_back.dto.AveriaDTO;
import edu.pucp.plg_back.dto.PedidoDTO;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.simulation.DiarioService;
import edu.pucp.plg_back.simulation.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SimulacionWSController {

    @Autowired
    private DiarioService diarioService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/simulation/start")
    public void startSimulation(String fechaHoraInicio) {
        // Lógica para iniciar la simulación en DiarioService
        try {
            diarioService.iniciarSimulacion(fechaHoraInicio);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // diarioService.iniciarSimulacion(fechaHoraInicio);
    }

    @MessageMapping("/simulation/continue")
    public void continueSimulation() {
        diarioService.signalToContinue();
    }

    @MessageMapping("/simulation/stop")
    public void stopSimulation() {
        diarioService.stopSimulacion();
    }
}