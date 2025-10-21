package chat.jace.dto.ws;

import chat.jace.domain.enums.PresenceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresencePayload {
    @NotNull(message = "Status is required")
    private PresenceStatus status;
}