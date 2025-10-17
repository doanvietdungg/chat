package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Reaction.ReactionId.class)
public class Reaction {
    @Id
    private UUID messageId;
    @Id
    private UUID userId;

    @Id
    @Column(length = 64)
    private String emoji;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionId implements Serializable {
        private UUID messageId;
        private UUID userId;
        private String emoji;
    }
}
