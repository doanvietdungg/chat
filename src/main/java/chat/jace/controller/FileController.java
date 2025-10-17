package chat.jace.controller;

import chat.jace.domain.FileResource;
import chat.jace.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService storageService;

    @Value("${spring.application.file-storage.local-path:./uploads}")
    private String localPath;

    @PostMapping("/upload")
    public ResponseEntity<FileResource> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // TODO: add type validation and size checks beyond Spring limits
        return ResponseEntity.ok(storageService.save(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResource> get(@PathVariable UUID id) {
        return storageService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Raw file serving; storedName is the filename part saved in url (after /files/)
    @GetMapping("/raw/{storedName:.+}")
    public void downloadRaw(@PathVariable String storedName, HttpServletResponse response) throws IOException {
        // Use the same configured storage path as FileStorageService
        Path root = Paths.get(localPath).toAbsolutePath().normalize();
        Path target = root.resolve(storedName).normalize();
        if (!Files.exists(target)) {
            response.setStatus(404);
            return;
        }
        try (InputStream in = Files.newInputStream(target)) {
            response.setStatus(200);
            response.setHeader("Content-Disposition", "inline; filename=\"" + storedName + "\"");
            StreamUtils.copy(in, response.getOutputStream());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) throws IOException {
        storageService.delete(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
