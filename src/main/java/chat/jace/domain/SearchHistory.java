package chat.jace.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "search_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID searchedUserId;

    @Builder.Default
    private OffsetDateTime searchedAt = OffsetDateTime.now();

    // Index for efficient queries
    @Table(indexes = {
        @Index(name = "idx_search_history_user_searched", columnList = "userId, searchedAt DESC")
    })
    public static class SearchHistoryIndexes {}
}