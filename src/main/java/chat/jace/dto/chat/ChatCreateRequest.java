package chat.jace.dto.chat;

import chat.jace.domain.enums.ChatType;
import chat.jace.dto.participant.ParticipantAddRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChatCreateRequest {
    @NotNull
    private ChatType type;
    private String title;
    private String description;
    // Option B extensions
    private UUID otherUserId; // for PRIVATE chat de-dup and creation
    private List<ParticipantAddRequest> participants; // initial participants for GROUP/CHANNEL
}
