package edu.pucp.plg_back.service.fileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.pucp.plg_back.model.Averia;

public class AveriaService {

    public List<Averia> leerAverias(String path) throws IOException {
        List<Averia> averias = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path));
        for (String line : lines) {
            String[] parts = line.split("_");
            if (parts.length == 3) {
                String turno = parts[0];
                String codigo = parts[1];
                int tipo = Integer.parseInt(parts[2].replace("TI", ""));
                averias.add(new Averia(turno, codigo, tipo));
            }
        }
        return averias;
    }

    public List<Averia> leerAveriasFromContent(String content) {
        List<Averia> averias = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return averias;
        }

        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("_");
                if (parts.length == 3) {
                    String turno = parts[0];
                    String codigo = parts[1];
                    int tipo = Integer.parseInt(parts[2].replace("TI", ""));
                    averias.add(new Averia(turno, codigo, tipo));
                }
            }
        }
        return averias;
    }
}