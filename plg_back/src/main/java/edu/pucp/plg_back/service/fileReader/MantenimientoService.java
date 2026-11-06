package edu.pucp.plg_back.service.fileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MantenimientoService {

    public Map<String, List<LocalDate>> cargarMantenimiento(String filePath) throws IOException {
        Map<String, List<LocalDate>> camionesEnMantenimiento = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":");
                if (partes.length == 2) {
                    String fechaStr = partes[0];
                    String codCamion = partes[1];

                    LocalDate fecha = LocalDate.parse(fechaStr, formatter);
                    // Asegurarse de que la lista exista para el camión
                    camionesEnMantenimiento.computeIfAbsent(codCamion, k -> new ArrayList<>()).add(fecha);
                }
            }
        }

        return camionesEnMantenimiento;
    }

    public Map<String, List<LocalDate>> cargarMantenimientoFromContent(String content) {
        Map<String, List<LocalDate>> camionesEnMantenimiento = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (content == null || content.trim().isEmpty()) {
            return camionesEnMantenimiento;
        }

        try (BufferedReader br = new BufferedReader(new StringReader(content))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split(":");
                    if (partes.length == 2) {
                        String fechaStr = partes[0];
                        String codCamion = partes[1];

                        LocalDate fecha = LocalDate.parse(fechaStr, formatter);
                        // Asegurarse de que la lista exista para el camión
                        camionesEnMantenimiento.computeIfAbsent(codCamion, k -> new ArrayList<>()).add(fecha);
                    }
                }
            }
        } catch (IOException e) {
            // This shouldn't happen with StringReader, but handle it gracefully
            System.err.println("Error reading maintenance data from content: " + e.getMessage());
        }

        return camionesEnMantenimiento;
    }
}
