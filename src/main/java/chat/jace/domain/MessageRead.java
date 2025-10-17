package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_reads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MessageRead.MessageReadId.class)
public class MessageRead {
    @Id
    private UUID messageId;
    @Id
    private UUID userId;

    @Builder.Default
    private OffsetDateTime readAt = OffsetDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageReadId implements Serializable {
        private UUID messageId;
        private UUID userId;
    }
}
