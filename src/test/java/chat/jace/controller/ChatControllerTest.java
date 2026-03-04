package chat.jace.controller;

import chat.jace.domain.enums.ChatType;
import chat.jace.dto.chat.ChatCreateRequest;
import chat.jace.dto.chat.ChatResponse;
import chat.jace.dto.chat.ChatUpdateRequest;
import chat.jace.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false) // tắt JWT filter để test thuần controller
@DisplayName("ChatController")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private UUID chatId;
    private ChatResponse sampleChat;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        sampleChat = ChatResponse.builder()
                .id(chatId)
                .type(ChatType.GROUP)
                .title("Test Group")
                .description("Test Description")
                .createdBy(UUID.randomUUID())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/chats
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/chats")
    class ListChats {

        @Test
        @DisplayName("list - should return 200 with paged chats")
        void list_shouldReturn200_withPagedChats() throws Exception {
            // Arrange
            var page = new PageImpl<>(List.of(sampleChat), PageRequest.of(0, 10), 1);
            when(chatService.listMyChats(any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/v1/chats")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Lấy danh sách chat thành công"))
                    .andExpect(jsonPath("$.data.content[0].id").value(chatId.toString()))
                    .andExpect(jsonPath("$.data.content[0].title").value("Test Group"));

            verify(chatService).listMyChats(any());
        }

        @Test
        @DisplayName("list - should return empty page when no chats")
        void list_shouldReturnEmptyPage_whenNoChats() throws Exception {
            // Arrange
            var emptyPage = new PageImpl<ChatResponse>(List.of(), PageRequest.of(0, 10), 0);
            when(chatService.listMyChats(any())).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/chats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }
    }

    // ─────────────────────────────────────────────
    // POST /api/v1/chats
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/v1/chats")
    class CreateChat {

        @Test
        @DisplayName("create - should return 201 when group chat created successfully")
        void create_shouldReturn201_whenGroupChatCreated() throws Exception {
            // Arrange
            var request = new ChatCreateRequest();
            request.setType(ChatType.GROUP);
            request.setTitle("Test Group");
            request.setDescription("Test Description");

            when(chatService.create(any(ChatCreateRequest.class))).thenReturn(sampleChat);

            // Act & Assert
            mockMvc.perform(post("/api/v1/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Tạo chat thành công"))
                    .andExpect(jsonPath("$.data.id").value(chatId.toString()))
                    .andExpect(jsonPath("$.data.type").value("GROUP"));

            verify(chatService).create(any(ChatCreateRequest.class));
        }

        @Test
        @DisplayName("create - should return 201 when private chat created")
        void create_shouldReturn201_whenPrivateChatCreated() throws Exception {
            // Arrange
            var privateChat = ChatResponse.builder()
                    .id(UUID.randomUUID())
                    .type(ChatType.PRIVATE)
                    .createdBy(UUID.randomUUID())
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            var request = new ChatCreateRequest();
            request.setType(ChatType.PRIVATE);
            request.setOtherUserId(UUID.randomUUID());

            when(chatService.create(any(ChatCreateRequest.class))).thenReturn(privateChat);

            // Act & Assert
            mockMvc.perform(post("/api/v1/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.type").value("PRIVATE"));
        }

        @Test
        @DisplayName("create - should return 400 when type is missing")
        void create_shouldReturn400_whenTypeIsMissing() throws Exception {
            // Arrange — type là @NotNull
            var body = "{\"title\":\"No Type Chat\"}";

            // Act & Assert
            mockMvc.perform(post("/api/v1/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(chatService);
        }

        @Test
        @DisplayName("create - should return 400 when group has no title")
        void create_shouldReturn400_whenGroupHasNoTitle() throws Exception {
            // Arrange — service throws vì GROUP cần title
            var request = new ChatCreateRequest();
            request.setType(ChatType.GROUP);

            when(chatService.create(any())).thenThrow(
                    new IllegalArgumentException("Title is required for group or channel"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/chats")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/chats/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/v1/chats/{id}")
    class GetChat {

        @Test
        @DisplayName("get - should return 200 with chat details")
        void get_shouldReturn200_withChatDetails() throws Exception {
            // Arrange
            when(chatService.get(chatId)).thenReturn(sampleChat);

            // Act & Assert
            mockMvc.perform(get("/api/v1/chats/{id}", chatId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Lấy thông tin chat thành công"))
                    .andExpect(jsonPath("$.data.id").value(chatId.toString()))
                    .andExpect(jsonPath("$.data.title").value("Test Group"));

            verify(chatService).get(chatId);
        }

        @Test
        @DisplayName("get - should return 400 when chat not found")
        void get_shouldReturn400_whenChatNotFound() throws Exception {
            // Arrange
            var unknownId = UUID.randomUUID();
            when(chatService.get(unknownId)).thenThrow(new IllegalArgumentException("Chat not found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chats/{id}", unknownId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("get - should return 403 when user is not a participant")
        void get_shouldReturn403_whenNotParticipant() throws Exception {
            // Arrange
            when(chatService.get(chatId))
                    .thenThrow(new org.springframework.security.access.AccessDeniedException("Not a participant"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/chats/{id}", chatId))
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // PUT /api/v1/chats/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/v1/chats/{id}")
    class UpdateChat {

        @Test
        @DisplayName("update - should return 200 with updated chat")
        void update_shouldReturn200_withUpdatedChat() throws Exception {
            // Arrange
            var updated = ChatResponse.builder()
                    .id(chatId)
                    .type(ChatType.GROUP)
                    .title("Updated Title")
                    .description("Updated Desc")
                    .createdBy(UUID.randomUUID())
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            var request = new ChatUpdateRequest();
            request.setTitle("Updated Title");
            request.setDescription("Updated Desc");

            when(chatService.update(eq(chatId), any(ChatUpdateRequest.class))).thenReturn(updated);

            // Act & Assert
            mockMvc.perform(put("/api/v1/chats/{id}", chatId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Cập nhật chat thành công"))
                    .andExpect(jsonPath("$.data.title").value("Updated Title"));

            verify(chatService).update(eq(chatId), any(ChatUpdateRequest.class));
        }

        @Test
        @DisplayName("update - should return 400 when user is not admin or owner")
        void update_shouldReturn400_whenNotAdminOrOwner() throws Exception {
            // Arrange
            when(chatService.update(eq(chatId), any()))
                    .thenThrow(new IllegalArgumentException("Admin or owner required"));

            var request = new ChatUpdateRequest();
            request.setTitle("New Title");

            // Act & Assert
            mockMvc.perform(put("/api/v1/chats/{id}", chatId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/v1/chats/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/v1/chats/{id}")
    class DeleteOrLeaveChat {

        @Test
        @DisplayName("deleteOrLeave - should return 204 when owner deletes chat")
        void deleteOrLeave_shouldReturn204_whenOwnerDeletesChat() throws Exception {
            // Arrange
            doNothing().when(chatService).deleteOrLeave(chatId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/chats/{id}", chatId))
                    .andExpect(status().isNoContent());

            verify(chatService).deleteOrLeave(chatId);
        }

        @Test
        @DisplayName("deleteOrLeave - should return 204 when member leaves chat")
        void deleteOrLeave_shouldReturn204_whenMemberLeavesChat() throws Exception {
            doNothing().when(chatService).deleteOrLeave(chatId);

            mockMvc.perform(delete("/api/v1/chats/{id}", chatId))
                    .andExpect(status().isNoContent());

            verify(chatService, times(1)).deleteOrLeave(chatId);
        }

        @Test
        @DisplayName("deleteOrLeave - should return 400 when chat not found")
        void deleteOrLeave_shouldReturn400_whenChatNotFound() throws Exception {
            // Arrange
            var unknownId = UUID.randomUUID();
            doThrow(new IllegalArgumentException("Chat not found"))
                    .when(chatService).deleteOrLeave(unknownId);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/chats/{id}", unknownId))
                    .andExpect(status().isBadRequest());
        }
    }
}
