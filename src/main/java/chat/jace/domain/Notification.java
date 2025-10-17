package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String type;

    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(columnDefinition = "jsonb")
    private String data;

    @Builder.Default
    private boolean read = false;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
