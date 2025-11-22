package chat.jace.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatParticipantInfo {
    private UUID id;
    private String name;
    private String avatar;
}
