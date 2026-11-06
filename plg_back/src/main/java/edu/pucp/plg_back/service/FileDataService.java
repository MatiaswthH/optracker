package edu.pucp.plg_back.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileDataService {

    // Storage for uploaded files content
    private final Map<String, String> bloqueoFiles = new ConcurrentHashMap<>();
    private final Map<String, String> pedidoFiles = new ConcurrentHashMap<>();
    private String averiaFileContent = null;
    private String mantenimientoFileContent = null;

    // Store bloqueo file content
    public void storeBloqueoFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String content = readFileContent(file);
        bloqueoFiles.put(filename, content);
    }

    // Store pedido file content
    public void storePedidoFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String content = readFileContent(file);
        pedidoFiles.put(filename, content);
    }

    // Store averia file content
    public void storeAveriaFile(MultipartFile file) throws IOException {
        this.averiaFileContent = readFileContent(file);
    }

    // Store mantenimiento file content
    public void storeMantenimientoFile(MultipartFile file) throws IOException {
        this.mantenimientoFileContent = readFileContent(file);
    }

    // Get bloqueo files for specific months
    public Map<String, String> getBloqueoFilesForMonths(List<YearMonth> months) {
        Map<String, String> result = new HashMap<>();
        for (YearMonth month : months) {
            String expectedFilename = String.format("%04d%02d.bloqueos.txt", month.getYear(), month.getMonthValue());
            if (bloqueoFiles.containsKey(expectedFilename)) {
                result.put(expectedFilename, bloqueoFiles.get(expectedFilename));
            }
        }
        return result;
    }

    // Get pedido files for specific months
    public Map<String, String> getPedidoFilesForMonths(List<YearMonth> months) {
        Map<String, String> result = new HashMap<>();
        for (YearMonth month : months) {
            String expectedFilename = String.format("ventas%04d%02d.txt", month.getYear(), month.getMonthValue());
            if (pedidoFiles.containsKey(expectedFilename)) {
                result.put(expectedFilename, pedidoFiles.get(expectedFilename));
            }
        }
        return result;
    }

    // Get averia file content
    public String getAveriaFileContent() {
        return averiaFileContent;
    }

    // Get mantenimiento file content
    public String getMantenimientoFileContent() {
        return mantenimientoFileContent;
    }

    // Clear all stored data
    public void clearAll() {
        bloqueoFiles.clear();
        pedidoFiles.clear();
        averiaFileContent = null;
        mantenimientoFileContent = null;
    }

    // Helper method to read file content
    private String readFileContent(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
