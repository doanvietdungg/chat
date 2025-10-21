package chat.jace.dto.user;

import chat.jace.domain.enums.PresenceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class UserSearchResponse {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private PresenceStatus presenceStatus;
    private OffsetDateTime lastSeenAt;
    private boolean isContact;
    private Integer mutualContactsCount;
    private String displayName; // Custom name if it's a contact
}