package edu.pucp.plg_back.service.fileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.pucp.plg_back.model.Pedido;


public class PedidoService {

    private static final int MAX_GLPMANAGEABLE = 200; // Capacidad máxima de la flota en m³

    public List<Pedido> leerPedidos(String filePath) {
        List<Pedido> pedidos = new ArrayList<>();
        double totalGLP = 0.0;

        // Expresión regular para procesar las líneas del archivo
        String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
        Pattern pattern = Pattern.compile(regex);

        try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(linea);
                if (matcher.matches()) {
                    // Extraer datos del pedido
                    int dia = Integer.parseInt(matcher.group(1));
                    int hora = Integer.parseInt(matcher.group(2));
                    int minuto = Integer.parseInt(matcher.group(3));
                    int posX = Integer.parseInt(matcher.group(4));
                    int posY = Integer.parseInt(matcher.group(5));
                    String idCliente = "c-" + matcher.group(6);
                    int volumen = Integer.parseInt(matcher.group(7));
                    int horasLimite = Integer.parseInt(matcher.group(8));

                    // Calcular fecha de pedido y fecha límite
                    LocalDateTime fechaPedido = LocalDateTime.of(2025, 5, dia, hora, minuto);
                    LocalDateTime fechaLimite = fechaPedido.plusHours(horasLimite);

                    // Crear el pedido
                    Pedido pedido = new Pedido(pedidos.size() + 1, idCliente, volumen, posX, posY, fechaPedido, fechaLimite, horasLimite);

                    // Verificar si el pedido puede ser manejado por la flota
                    if (totalGLP + volumen <= MAX_GLPMANAGEABLE) {
                        pedidos.add(pedido);
                        totalGLP += volumen;
                    } else {
                        System.out.printf("Pedido %s no asignado: excede la capacidad máxima de la flota.\n", idCliente);
                    }
                } else {
                    System.err.printf("Línea con formato incorrecto: %s\n", linea);
                }
            }
        } catch (IOException e) {
            System.err.printf("Error al leer el archivo: %s\n", filePath);
        }

        // Asignar pedidos a los camiones utilizando la lógica FCFS
        return pedidos;
    }

    
}