package chat.jace.dto.contact;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ContactRequest {
    @NotNull(message = "Contact user ID is required")
    private UUID contactUserId;
    
    private String displayName; // Optional custom name
}