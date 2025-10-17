package chat.jace.dto.chat;

import chat.jace.domain.enums.ChatType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatCreateRequest {
    @NotNull
    private ChatType type;
    private String title;
    private String description;
}
