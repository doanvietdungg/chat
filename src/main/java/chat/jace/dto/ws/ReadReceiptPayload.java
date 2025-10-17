package chat.jace.dto.ws;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReadReceiptPayload {
    @NotNull
    private UUID chatId;

    @NotNull
    private UUID messageId;
}
