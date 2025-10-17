package chat.jace.dto.participant;

import chat.jace.domain.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ParticipantAddRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private ParticipantRole role = ParticipantRole.MEMBER;
}
