package chat.jace.repository;

import chat.jace.domain.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    
    @Query("""
        SELECT DISTINCT sh.searchedUserId 
        FROM SearchHistory sh 
        WHERE sh.userId = :userId 
        ORDER BY sh.searchedAt DESC
        """)
    List<UUID> findRecentSearchedUserIds(@Param("userId") UUID userId, Pageable pageable);
    
    void deleteByUserIdAndSearchedUserId(UUID userId, UUID searchedUserId);
    
    void deleteByUserIdAndSearchedAtBefore(UUID userId, OffsetDateTime before);
    
    boolean existsByUserIdAndSearchedUserId(UUID userId, UUID searchedUserId);
}