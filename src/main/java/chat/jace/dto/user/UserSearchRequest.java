package chat.jace.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSearchRequest {
    @NotBlank(message = "Search query is required")
    @Size(min = 1, max = 100, message = "Search query must be between 1 and 100 characters")
    private String query;
    
    private Integer limit = 10; // Default limit
}