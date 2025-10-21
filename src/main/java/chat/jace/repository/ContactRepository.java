package chat.jace.repository;

import chat.jace.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    
    Optional<Contact> findByUserIdAndContactUserId(UUID userId, UUID contactUserId);
    
    Page<Contact> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);
    
    List<Contact> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    
    boolean existsByUserIdAndContactUserId(UUID userId, UUID contactUserId);
    
    void deleteByUserIdAndContactUserId(UUID userId, UUID contactUserId);
    
    @Query("SELECT COUNT(c) FROM Contact c WHERE c.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);
    
    // Count mutual contacts between two users
    @Query("""
        SELECT COUNT(DISTINCT c1.contactUserId) 
        FROM Contact c1 
        JOIN Contact c2 ON c1.contactUserId = c2.contactUserId 
        WHERE c1.userId = :userId1 AND c2.userId = :userId2
        """)
    Integer countMutualContacts(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
    
    // Get mutual contact user IDs
    @Query("""
        SELECT DISTINCT c1.contactUserId 
        FROM Contact c1 
        JOIN Contact c2 ON c1.contactUserId = c2.contactUserId 
        WHERE c1.userId = :userId1 AND c2.userId = :userId2
        """)
    List<UUID> findMutualContactIds(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}