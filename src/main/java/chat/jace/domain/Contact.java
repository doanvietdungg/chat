package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID contactUserId;

    private String displayName; // Custom name for the contact

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Unique constraint to prevent duplicate contacts
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "contactUserId"})
    })
    public static class ContactConstraints {}
}