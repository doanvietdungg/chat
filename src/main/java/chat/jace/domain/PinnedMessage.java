package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pinned_messages", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "message_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinnedMessage {
    
    @EmbeddedId
    private PinnedMessageId id;
    
    @Column(nullable = false)
    private UUID pinnedBy;
    
    @Builder.Default
    private OffsetDateTime pinnedAt = OffsetDateTime.now();
    
    // Optional: order for displaying multiple pinned messages
    private Integer displayOrder;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PinnedMessageId implements Serializable {
        @Column(name = "chat_id")
        private UUID chatId;
        
        @Column(name = "message_id")
        private UUID messageId;
    }
}
