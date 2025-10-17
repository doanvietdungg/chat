package chat.jace.dto.participant;

import chat.jace.domain.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParticipantUpdateRequest {
    @NotNull
    private ParticipantRole role;
}
