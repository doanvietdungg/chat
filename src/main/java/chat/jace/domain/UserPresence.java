package chat.jace.domain;

import chat.jace.domain.enums.PresenceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_presence")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresence {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PresenceStatus status = PresenceStatus.OFFLINE;

    @Builder.Default
    private OffsetDateTime lastSeenAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}