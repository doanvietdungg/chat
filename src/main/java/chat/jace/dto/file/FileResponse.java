package chat.jace.dto.file;

import chat.jace.domain.enums.FileOwnerType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class FileResponse {
    private UUID id;
    private String name;
    private Long size;
    private String contentType;
    private String url;
    private UUID uploadedBy;
    private FileOwnerType ownerType;
    private UUID ownerId;
    private OffsetDateTime createdAt;
}
