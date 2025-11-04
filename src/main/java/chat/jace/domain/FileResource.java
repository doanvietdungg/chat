package chat.jace.domain;

import chat.jace.domain.enums.FileOwnerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResource {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    private String contentType;

    @Column(nullable = false, length = 1024)
    private String url;

    private UUID uploadedBy;

    // Polymorphic association fields
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private FileOwnerType ownerType = FileOwnerType.NONE;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
