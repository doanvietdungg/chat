package chat.jace.dto.ws;

import chat.jace.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessagePayload {
    @NotNull
    private UUID chatId;

    @NotBlank
    private String text;

    @NotNull
    private MessageType type = MessageType.TEXT;
}
