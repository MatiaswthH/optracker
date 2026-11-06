package edu.pucp.plg_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.pucp.plg_back.dto.AveriaRequest;
import edu.pucp.plg_back.simulation.DiarioService;

@RestController
@RequestMapping("/diarioAverias")
public class AveriaDiarioController {
    @Autowired
    private DiarioService diarioService;

    @PostMapping("/averia")
    public ResponseEntity<?> registrarAveria(@RequestBody AveriaRequest request) {
        diarioService.registrarAveriaSolicitada(request);
        return ResponseEntity.ok().build();
    }
}
