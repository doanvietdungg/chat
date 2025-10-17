package chat.jace.domain;

import chat.jace.domain.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Participant.ParticipantId.class)
public class Participant {
    @Id
    private UUID chatId;
    @Id
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role;

    @Builder.Default
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantId implements Serializable {
        private UUID chatId;
        private UUID userId;
    }
}
