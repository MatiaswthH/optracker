package edu.pucp.plg_back.service.fileReader;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Nodo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@Setter
public class BloqueoService {

    // private List<Bloqueo> bloqueos = new ArrayList<>();

    public List<Bloqueo> leerBloqueos(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws IOException {

        // Logica para leer los archivos necesarios dado fecha inicio y fin
        List<Bloqueo> bloqueos = new ArrayList<>();
        String directorioBloqueos = new File("").getAbsolutePath() + "/../data/bloqueos";

        List<YearMonth> meses = new ArrayList<>();
        YearMonth inicio = YearMonth.from(fechaInicio);
        YearMonth fin = YearMonth.from(fechaFin);
        YearMonth actual = inicio;
        while (!actual.isAfter(fin)) {
            meses.add(actual);
            actual = actual.plusMonths(1);
        }

        List<File> archivosALeer = new ArrayList<>();
        for (YearMonth ym : meses) {
            String nombreArchivo = String.format("%04d%02d.bloqueos.txt", ym.getYear(), ym.getMonthValue());
            File archivo = new File(directorioBloqueos, nombreArchivo);
            if (archivo.exists()) {
                archivosALeer.add(archivo);
            }
        }

        // Logica para parsear lo leido a List<Bloqueos>

        for (File archivo : archivosALeer) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    // Separar la línea en el período y los tramos bloqueados
                    String[] partes = linea.split(":");
                    String rango_fechas = partes[0];
                    String coordenadas = partes[1];

                    // Parsear el período de tiempo de cada tramo bloqueado
                    String nombreArchivo = archivo.getName();
                    String regexAnhoMes = "(\\d{4})(\\d{2})\\.bloqueos\\.txt";
                    Pattern patternAnhoMes = Pattern.compile(regexAnhoMes);
                    Matcher matcherAnhoMes = patternAnhoMes.matcher(nombreArchivo);

                    int anho = 2025, mes = 1; // valores por defecto
                    if (matcherAnhoMes.matches()) {
                        anho = Integer.parseInt(matcherAnhoMes.group(1));
                        mes = Integer.parseInt(matcherAnhoMes.group(2));
                    }
                    String[] tiempos = rango_fechas.split("-");
                    LocalDateTime ini = parsearFecha(tiempos[0], anho, mes);
                    LocalDateTime end = parsearFecha(tiempos[1], anho, mes);

                    if (!ini.isBefore(fechaInicio) && !end.isAfter(fechaFin)) {

                        // Parsear las coordenadas dadas
                        String[] puntos = coordenadas.split(",");
                        List<Nodo> puntosDados = new ArrayList<>();
                        for (int i = 0; i < puntos.length; i += 2) {
                            // Tomo de 2 en 2 porque las coordenadas están en pares (x, y)
                            int x = Integer.parseInt(puntos[i]);
                            int y = Integer.parseInt(puntos[i + 1]);
                            puntosDados.add(new Nodo(x, y));
                        }

                        // Completar todos los nodos de los tramos (estos nunca serán diagonales)
                        List<Nodo> tramos = new ArrayList<>();
                        for (int i = 0; i < puntosDados.size() - 1; i++) {
                            Nodo inicioTramo = puntosDados.get(i);
                            Nodo finTramo = puntosDados.get(i + 1);

                            if (inicioTramo.getX() == finTramo.getX()) {
                                // Tramo vertical
                                int x = inicioTramo.getX();
                                int yStart = inicioTramo.getY();
                                int yEnd = finTramo.getY();
                                int step = yStart <= yEnd ? 1 : -1;
                                // Si no es el primer tramo, empieza desde yStart + step para evitar duplicado
                                int startY = (i == 0) ? yStart : yStart + step;
                                for (int y = startY; (step > 0 ? y <= yEnd : y >= yEnd); y += step) {
                                    Nodo nodo = new Nodo(x, y);
                                    nodo.setInicioBloqueo(ini);
                                    nodo.setFinBloqueo(end);
                                    nodo.setEstaBloqueado(true);
                                    tramos.add(nodo);
                                }
                            } else if (inicioTramo.getY() == finTramo.getY()) {
                                // Tramo horizontal
                                int y = inicioTramo.getY();
                                int xStart = inicioTramo.getX();
                                int xEnd = finTramo.getX();
                                int step = xStart <= xEnd ? 1 : -1;
                                // Si no es el primer tramo, empieza desde xStart + step para evitar duplicado
                                int startX = (i == 0) ? xStart : xStart + step;
                                for (int x = startX; (step > 0 ? x <= xEnd : x >= xEnd); x += step) {
                                    Nodo nodo = new Nodo(x, y);
                                    nodo.setInicioBloqueo(ini);
                                    nodo.setFinBloqueo(end);
                                    nodo.setEstaBloqueado(true);
                                    tramos.add(nodo);
                                }
                            }
                            // Si no es ni horizontal ni vertical, no se agrega (según tu requerimiento)
                        }
                        /*
                         * for (int i = 0; i < puntos.length; i += 2) {
                         * int x = Integer.parseInt(puntos[i]);
                         * int y = Integer.parseInt(puntos[i + 1]);
                         * Nodo nodo = new Nodo(x, y);
                         * nodo.setInicioBloqueo(ini);
                         * nodo.setFinBloqueo(end);
                         * nodo.setEstaBloqueado(true); // Marcar el nodo como bloqueado
                         * tramos.add(nodo);
                         * }
                         */

                        int aaPeriodo = ini.getYear();
                        int mmPeriodo = ini.getMonthValue();
                        String periodo = Integer.toString(aaPeriodo) + Integer.toString(mmPeriodo);
                        // Crear el objeto Bloqueo y agregarlo a la lista
                        bloqueos.add(new Bloqueo(bloqueos.size() + 1, tramos, periodo, "Bloqueo de ruta", ini, end));
                    }
                }
            }
        }

        return bloqueos;
    }

    public List<Bloqueo> leerBloqueosFromContent(Map<String, String> fileContents, LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        List<Bloqueo> bloqueos = new ArrayList<>();

        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            String nombreArchivo = entry.getKey();
            String contenido = entry.getValue();

            // Parsear el nombre del archivo para obtener año y mes
            String regexAnhoMes = "(\\d{4})(\\d{2})\\.bloqueos\\.txt";
            Pattern patternAnhoMes = Pattern.compile(regexAnhoMes);
            Matcher matcherAnhoMes = patternAnhoMes.matcher(nombreArchivo);

            int anho = 2025, mes = 1; // valores por defecto
            if (matcherAnhoMes.matches()) {
                anho = Integer.parseInt(matcherAnhoMes.group(1));
                mes = Integer.parseInt(matcherAnhoMes.group(2));
            }

            try (BufferedReader br = new BufferedReader(new StringReader(contenido))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty())
                        continue;

                    // Separar la línea en el período y los tramos bloqueados
                    String[] partes = linea.split(":");
                    if (partes.length < 2)
                        continue;

                    String rango_fechas = partes[0];
                    String coordenadas = partes[1];

                    // Parsear el período de tiempo de cada tramo bloqueado
                    String[] tiempos = rango_fechas.split("-");
                    LocalDateTime ini = parsearFecha(tiempos[0], anho, mes);
                    LocalDateTime end = parsearFecha(tiempos[1], anho, mes);

                    if (!ini.isBefore(fechaInicio) && !end.isAfter(fechaFin)) {

                        // Parsear las coordenadas dadas
                        String[] puntos = coordenadas.split(",");
                        List<Nodo> puntosDados = new ArrayList<>();
                        for (int i = 0; i < puntos.length; i += 2) {
                            // Tomo de 2 en 2 porque las coordenadas están en pares (x, y)
                            int x = Integer.parseInt(puntos[i]);
                            int y = Integer.parseInt(puntos[i + 1]);
                            puntosDados.add(new Nodo(x, y));
                        }

                        // Completar todos los nodos de los tramos (estos nunca serán diagonales)
                        List<Nodo> tramos = new ArrayList<>();
                        for (int i = 0; i < puntosDados.size() - 1; i++) {
                            Nodo inicioTramo = puntosDados.get(i);
                            Nodo finTramo = puntosDados.get(i + 1);

                            if (inicioTramo.getX() == finTramo.getX()) {
                                // Tramo vertical
                                int x = inicioTramo.getX();
                                int yStart = inicioTramo.getY();
                                int yEnd = finTramo.getY();
                                int step = yStart <= yEnd ? 1 : -1;
                                // Si no es el primer tramo, empieza desde yStart + step para evitar duplicado
                                int startY = (i == 0) ? yStart : yStart + step;
                                for (int y = startY; (step > 0 ? y <= yEnd : y >= yEnd); y += step) {
                                    Nodo nodo = new Nodo(x, y);
                                    nodo.setInicioBloqueo(ini);
                                    nodo.setFinBloqueo(end);
                                    nodo.setEstaBloqueado(true);
                                    tramos.add(nodo);
                                }
                            } else if (inicioTramo.getY() == finTramo.getY()) {
                                // Tramo horizontal
                                int y = inicioTramo.getY();
                                int xStart = inicioTramo.getX();
                                int xEnd = finTramo.getX();
                                int step = xStart <= xEnd ? 1 : -1;
                                // Si no es el primer tramo, empieza desde xStart + step para evitar duplicado
                                int startX = (i == 0) ? xStart : xStart + step;
                                for (int x = startX; (step > 0 ? x <= xEnd : x >= xEnd); x += step) {
                                    Nodo nodo = new Nodo(x, y);
                                    nodo.setInicioBloqueo(ini);
                                    nodo.setFinBloqueo(end);
                                    nodo.setEstaBloqueado(true);
                                    tramos.add(nodo);
                                }
                            }
                            // Si no es ni horizontal ni vertical, no se agrega (según tu requerimiento)
                        }

                        int aaPeriodo = ini.getYear();
                        int mmPeriodo = ini.getMonthValue();
                        String periodo = Integer.toString(aaPeriodo) + Integer.toString(mmPeriodo);
                        // Crear el objeto Bloqueo y agregarlo a la lista
                        bloqueos.add(new Bloqueo(bloqueos.size() + 1, tramos, periodo, "Bloqueo de ruta", ini, end));
                    }
                }
            } catch (IOException e) {
                // This shouldn't happen with StringReader, but handle it gracefully
                System.err.println("Error reading bloqueo data from content: " + e.getMessage());
            }
        }

        return bloqueos;
    }

    private LocalDateTime parsearFecha(String tiempo, int anho, int mes) {
        String dias = tiempo.substring(0, 2);
        String horas = tiempo.substring(3, 5);
        String minutos = tiempo.substring(6, 8);

        int dia = Integer.parseInt(dias);
        int hora = Integer.parseInt(horas);
        int minuto = Integer.parseInt(minutos);

        return LocalDateTime.of(anho, mes, dia, hora, minuto);
    }
}
