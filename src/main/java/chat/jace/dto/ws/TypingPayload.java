package chat.jace.dto.ws;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TypingPayload {
    @NotNull
    private UUID chatId;

    @NotNull
    private Boolean typing; // true=start, false=stop
}
