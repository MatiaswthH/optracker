package edu.pucp.plg_back.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.simulation.DiarioService;

@RestController
@RequestMapping("/diarioPedidos")
public class PedidoDiarioController {
    private final DiarioService diarioService;

    public PedidoDiarioController(DiarioService diarioService) {
        this.diarioService = diarioService;
    }

    @PostMapping("/archivo-pedidos")
    public ResponseEntity<String> uploadPedidosFile(@RequestParam("file") MultipartFile file,
            @RequestParam("fechaPedido") String fechaPedidoStr) {
        try {
            List<Pedido> pedidos = new ArrayList<>();
            String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
            Pattern pattern = Pattern.compile(regex);

            LocalDateTime fechaPedido;
            if ("AUTO".equals(fechaPedidoStr)) {
                fechaPedido = diarioService.getFechaActualSimulacion(); // Use the current simulation date
            } else {
                fechaPedido = LocalDateTime.parse(fechaPedidoStr);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty())
                        continue;
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        int posX = Integer.parseInt(matcher.group(4));
                        int posY = Integer.parseInt(matcher.group(5));
                        String idCliente = "c-" + matcher.group(6);
                        int volumen = Integer.parseInt(matcher.group(7));
                        int horasLimite = Integer.parseInt(matcher.group(8));
                        Pedido pedido = new Pedido();
                        pedido.setClienteId(idCliente);
                        pedido.setVolumen(volumen);
                        pedido.setX(posX);
                        pedido.setY(posY);
                        pedido.setFechaPedido(fechaPedido);
                        pedido.setFechaLimiteEntrega(fechaPedido.plusHours(horasLimite));
                        pedidos.add(pedido);
                    } else {
                        System.err.printf("Línea con formato incorrecto: %s\n", line);
                    }
                }
            }
            for (Pedido pedido : pedidos) {
                diarioService.addPedido(pedido);
            }
            return ResponseEntity.ok("Archivo procesado y pedidos agregados");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar archivo");
        }
    }
}
