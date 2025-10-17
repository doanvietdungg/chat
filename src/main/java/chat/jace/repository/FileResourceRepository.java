package chat.jace.repository;

import chat.jace.domain.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileResourceRepository extends JpaRepository<FileResource, UUID> {
}
