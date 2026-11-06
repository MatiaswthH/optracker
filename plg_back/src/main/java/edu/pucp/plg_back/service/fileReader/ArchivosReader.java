package edu.pucp.plg_back.service.fileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pucp.plg_back.model.Pedido;

public class ArchivosReader {

    public List<Pedido> leerPedidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        List<Pedido> pedidos = new ArrayList<>();

        // Expresión regular para procesar las líneas del archivo
        String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
        Pattern pattern = Pattern.compile(regex);

        // Ruta del directorio donde están los archivos de pedidos a leer
        String directorioPedidos = new File("").getAbsolutePath() +
                "/../data/pedidos";

        // 1. Determinar los meses que se leeran
        List<YearMonth> meses = new ArrayList<>();
        YearMonth inicio = YearMonth.from(fechaInicio);
        YearMonth fin = YearMonth.from(fechaFin);
        YearMonth actual = inicio;
        while (!actual.isAfter(fin)) {
            meses.add(actual);
            actual = actual.plusMonths(1);
        }

        // 2. Construir los nombres de archivos a leer
        List<File> archivosALeer = new ArrayList<>();
        for (YearMonth ym : meses) {
            String nombreArchivo = String.format("ventas%04d%02d.txt", ym.getYear(), ym.getMonthValue());
            File archivo = new File(directorioPedidos, nombreArchivo);
            if (archivo.exists()) {
                archivosALeer.add(archivo);
            }
        }

        if (archivosALeer.isEmpty()) {
            System.err.println("No se encontraron archivos en el rango solicitado.");
            return pedidos;
        }

        // 3. Leer solo los archivos necesarios y guardar los pedidos
        for (File archivo : archivosALeer) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(linea);
                    if (matcher.matches()) {
                        int dia = Integer.parseInt(matcher.group(1));
                        int hora = Integer.parseInt(matcher.group(2));
                        int minuto = Integer.parseInt(matcher.group(3));
                        int posX = Integer.parseInt(matcher.group(4));
                        int posY = Integer.parseInt(matcher.group(5));
                        String idCliente = "c-" + matcher.group(6);
                        int volumen = Integer.parseInt(matcher.group(7));
                        int horasLimite = Integer.parseInt(matcher.group(8));

                        // Extraer año y mes del nombre del archivo
                        String nombreArchivo = archivo.getName();
                        String regexAnhoMes = "ventas(\\d{4})(\\d{2})\\.txt";
                        Pattern patternAnhoMes = Pattern.compile(regexAnhoMes);
                        Matcher matcherAnhoMes = patternAnhoMes.matcher(nombreArchivo);

                        if (matcherAnhoMes.matches()) {
                            int anho = Integer.parseInt(matcherAnhoMes.group(1));
                            int mes = Integer.parseInt(matcherAnhoMes.group(2));
                            LocalDateTime fechaPedido = LocalDateTime.of(anho, mes, dia, hora, minuto);
                            LocalDateTime fechaLimite = fechaPedido.plusHours(horasLimite);

                            // Si el pedido esta en el rango de fechas dada
                            if (!fechaPedido.isBefore(fechaInicio) && !fechaPedido.isAfter(fechaFin)) {
                                Pedido pedido = new Pedido(pedidos.size() + 1, idCliente, volumen, posX, posY,
                                        fechaPedido, fechaLimite, horasLimite);
                                pedidos.add(pedido);
                            }
                        }
                    } else {
                        System.err.printf("Línea con formato incorrecto: %s\n", linea);
                    }
                }
            } catch (IOException e) {
                System.err.printf("Error al leer el archivo: %s\n", archivo.getName());
            }
        }
        return pedidos;
    }

    public List<Pedido> leerPedidosFromContent(Map<String, String> fileContents, LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        List<Pedido> pedidos = new ArrayList<>();

        // Expresión regular para procesar las líneas del archivo
        String regex = "(\\d+)d(\\d+)h(\\d+)m:(\\d+),(\\d+),c-(\\d+),(\\d+)m3,(\\d+)h";
        Pattern pattern = Pattern.compile(regex);

        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            String nombreArchivo = entry.getKey();
            String contenido = entry.getValue();

            // Extraer año y mes del nombre del archivo
            String regexAnhoMes = "ventas(\\d{4})(\\d{2})\\.txt";
            Pattern patternAnhoMes = Pattern.compile(regexAnhoMes);
            Matcher matcherAnhoMes = patternAnhoMes.matcher(nombreArchivo);

            if (matcherAnhoMes.matches()) {
                int anho = Integer.parseInt(matcherAnhoMes.group(1));
                int mes = Integer.parseInt(matcherAnhoMes.group(2));

                try (BufferedReader br = new BufferedReader(new StringReader(contenido))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        linea = linea.trim();
                        if (linea.isEmpty())
                            continue;

                        Matcher matcher = pattern.matcher(linea);
                        if (matcher.matches()) {
                            int dia = Integer.parseInt(matcher.group(1));
                            int hora = Integer.parseInt(matcher.group(2));
                            int minuto = Integer.parseInt(matcher.group(3));
                            int posX = Integer.parseInt(matcher.group(4));
                            int posY = Integer.parseInt(matcher.group(5));
                            String idCliente = "c-" + matcher.group(6);
                            int volumen = Integer.parseInt(matcher.group(7));
                            int horasLimite = Integer.parseInt(matcher.group(8));

                            LocalDateTime fechaPedido = LocalDateTime.of(anho, mes, dia, hora, minuto);
                            LocalDateTime fechaLimite = fechaPedido.plusHours(horasLimite);

                            // Si el pedido esta en el rango de fechas dada
                            if (!fechaPedido.isBefore(fechaInicio) && !fechaPedido.isAfter(fechaFin)) {
                                Pedido pedido = new Pedido(pedidos.size() + 1, idCliente, volumen, posX, posY,
                                        fechaPedido, fechaLimite, horasLimite);
                                pedidos.add(pedido);
                            }
                        } else {
                            System.err.printf("Línea con formato incorrecto: %s\n", linea);
                        }
                    }
                } catch (IOException e) {
                    // This shouldn't happen with StringReader, but handle it gracefully
                    System.err.printf("Error reading pedido data from content: %s\n", e.getMessage());
                }
            }
        }
        return pedidos;
    }
}
