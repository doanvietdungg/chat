package chat.jace.dto.chat;

import lombok.Data;

@Data
public class ChatUpdateRequest {
    private String title;
    private String description;
    private String settings; // JSON string
}
