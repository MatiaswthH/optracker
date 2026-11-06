package edu.pucp.plg_back.controller;

import edu.pucp.plg_back.util.FileLogger;
import edu.pucp.plg_back.service.FileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bloqueos")
public class BloqueoController {

    @Autowired
    private FileDataService fileDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadBloqueos(@RequestParam("file") MultipartFile file) {
        try {
            FileLogger.log("Attempting to upload bloqueos file.");
            if (file.isEmpty()) {
                FileLogger.log("Upload failed: File was empty.");
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".txt")) {
                FileLogger.log("Upload failed: Invalid file type for file: " + originalFilename);
                return ResponseEntity.badRequest().body("El archivo debe ser un archivo .txt");
            }
            // Store file content in FileDataService
            fileDataService.storeBloqueoFile(file);
            FileLogger.log("Archivo de bloqueos recibido y almacenado: " + originalFilename);
            FileLogger.log("Tamaño del archivo: " + file.getSize() + " bytes");

            return ResponseEntity.ok("Archivo de bloqueos subido exitosamente: " + originalFilename);
        } catch (Exception e) {
            FileLogger.log("Error al procesar el archivo de bloqueos: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al procesar el archivo: " + e.getMessage());
        }
    }
}
