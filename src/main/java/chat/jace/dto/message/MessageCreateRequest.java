package chat.jace.dto.message;

import chat.jace.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageCreateRequest {
    @NotNull
    private UUID chatId;

    @NotBlank
    private String text;

    @NotNull
    private MessageType type = MessageType.TEXT;
}
