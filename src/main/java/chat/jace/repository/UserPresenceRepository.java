package chat.jace.repository;

import chat.jace.domain.UserPresence;
import chat.jace.domain.enums.PresenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, UUID> {
    
    Optional<UserPresence> findByUserId(UUID userId);
    
    List<UserPresence> findByUserIdIn(List<UUID> userIds);
    
    @Modifying
    @Query("UPDATE UserPresence up SET up.status = :status, up.updatedAt = :updatedAt WHERE up.userId = :userId")
    void updateStatusByUserId(@Param("userId") UUID userId, 
                             @Param("status") PresenceStatus status, 
                             @Param("updatedAt") OffsetDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE UserPresence up SET up.lastSeenAt = :lastSeenAt, up.updatedAt = :updatedAt WHERE up.userId = :userId")
    void updateLastSeenByUserId(@Param("userId") UUID userId, 
                               @Param("lastSeenAt") OffsetDateTime lastSeenAt,
                               @Param("updatedAt") OffsetDateTime updatedAt);
}