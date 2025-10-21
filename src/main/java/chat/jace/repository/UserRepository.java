package chat.jace.repository;

import chat.jace.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Search users by username or email (case-insensitive)
    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) 
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY 
            CASE WHEN LOWER(u.username) = LOWER(:query) THEN 1 ELSE 2 END,
            CASE WHEN LOWER(u.email) = LOWER(:query) THEN 1 ELSE 2 END,
            u.username ASC
        """)
    List<User> searchByUsernameOrEmail(@Param("query") String query, Pageable pageable);
    
    // Find users by IDs maintaining order
    @Query("SELECT u FROM User u WHERE u.id IN :ids ORDER BY u.username ASC")
    List<User> findByIdInOrderByUsername(@Param("ids") List<UUID> ids);
    
    // Get suggested contacts (users who are not already contacts)
    @Query("""
        SELECT u FROM User u 
        WHERE u.id != :currentUserId 
        AND u.id NOT IN (
            SELECT c.contactUserId FROM Contact c WHERE c.userId = :currentUserId
        )
        ORDER BY u.username ASC
        """)
    List<User> findSuggestedContacts(@Param("currentUserId") UUID currentUserId, Pageable pageable);
}
