package chat.jace.service;

import chat.jace.domain.FileResource;
import chat.jace.domain.enums.FileOwnerType;
import chat.jace.repository.FileResourceRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${spring.application.file-storage.local-path:./uploads}")
    private String localPath;

    @Value("${spring.application.base-url:http://localhost:8080}")
    private String baseUrl;

    private final FileResourceRepository fileRepo;

    @Transactional
    public FileResource save(MultipartFile file) throws IOException {
        UUID me = SecurityUtils.currentUserIdOrThrow();
        String originalName = StringUtils.cleanPath(Optional.ofNullable(file.getOriginalFilename()).orElse("file"));
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot);
        String storedName = UUID.randomUUID() + "-" + Instant.now().toEpochMilli() + ext;

        Path root = Paths.get(localPath).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path target = root.resolve(storedName).normalize();
        Files.copy(file.getInputStream(), target);

        FileResource fr = FileResource.builder()
                .name(originalName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .url(baseUrl + "/api/v1/files/raw/" + storedName)
                .uploadedBy(me)
                .build();
        return fileRepo.save(fr);
    }

    public Optional<FileResource> get(UUID id) {
        return fileRepo.findById(id);
    }

    @Transactional
    public void delete(UUID id) throws IOException {
        var fr = fileRepo.findById(id).orElseThrow();
        fileRepo.deleteById(id);
        
        // Best effort to remove local file (parse filename from URL)
        if (fr.getUrl() != null) {
            String storedName = extractStoredName(fr.getUrl());
            if (storedName != null) {
                Path root = Paths.get(localPath).toAbsolutePath().normalize();
                Path target = root.resolve(storedName).normalize();
                Files.deleteIfExists(target);
            }
        }
    }
    
    private String extractStoredName(String url) {
        // Handle both full URL and relative URL
        // Full: http://localhost:8080/api/v1/files/raw/abc123.jpg
        // Relative: /api/v1/files/raw/abc123.jpg
        if (url.contains("/files/raw/")) {
            int index = url.indexOf("/files/raw/");
            return url.substring(index + "/files/raw/".length());
        }
        return null;
    }

    /**
     * Set owner for a file (polymorphic association)
     * @param fileId File ID
     * @param ownerType Type of owner (MESSAGE, POST, etc.)
     * @param ownerId ID of the owner entity
     */
    @Transactional
    public void setOwner(UUID fileId, FileOwnerType ownerType, UUID ownerId) {
        FileResource file = fileRepo.findById(fileId).orElseThrow(
            () -> new IllegalArgumentException("File not found: " + fileId)
        );
        file.setOwnerType(ownerType);
        file.setOwnerId(ownerId);
        fileRepo.save(file);
    }
}
