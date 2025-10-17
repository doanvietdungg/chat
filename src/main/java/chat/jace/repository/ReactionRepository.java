package chat.jace.repository;

import chat.jace.domain.Reaction;
import chat.jace.domain.Reaction.ReactionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, ReactionId> {
}
