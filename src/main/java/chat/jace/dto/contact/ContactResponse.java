package chat.jace.dto.contact;

import chat.jace.domain.enums.PresenceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactResponse {
    private UUID id;
    private UUID userId;
    private UUID contactUserId;
    private String displayName;
    private String username;
    private String email;
    private String avatarUrl;
    private PresenceStatus presenceStatus;
    private OffsetDateTime lastSeenAt;
    private Integer mutualContactsCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}