package chat.jace.controller;

import chat.jace.dto.common.ApiResponse;
import chat.jace.dto.common.ResponseFactory;
import chat.jace.dto.message.PinMessageRequest;
import chat.jace.dto.message.PinnedMessageResponse;
import chat.jace.service.PinnedMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats/{chatId}/pinned-messages")
@RequiredArgsConstructor
@Tag(name = "Pinned Messages", description = "Pin and unpin messages in chats")
public class PinnedMessageController {

    private final PinnedMessageService pinnedMessageService;

    @PostMapping
    @Operation(summary = "Pin a message")
    public ApiResponse<PinnedMessageResponse> pinMessage(
            @PathVariable UUID chatId,
            @RequestBody PinMessageRequest request) {
        return ResponseFactory.success(
                pinnedMessageService.pinMessage(chatId, request.getMessageId(), request.getDisplayOrder())
        ).getBody();
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Unpin a message")
    public ResponseEntity<ApiResponse<Object>> unpinMessage(
            @PathVariable UUID chatId,
            @PathVariable UUID messageId) {
        pinnedMessageService.unpinMessage(chatId, messageId);
        return ResponseFactory.success(null);
    }

    @GetMapping
    @Operation(summary = "Get all pinned messages in a chat")
    public ApiResponse<List<PinnedMessageResponse>> getPinnedMessages(@PathVariable UUID chatId) {
        return ResponseFactory.success(pinnedMessageService.getPinnedMessages(chatId)).getBody();
    }

    @GetMapping("/{messageId}/is-pinned")
    @Operation(summary = "Check if a message is pinned")
    public ApiResponse<Boolean> isPinned(
            @PathVariable UUID chatId,
            @PathVariable UUID messageId) {
        return ResponseFactory.success(pinnedMessageService.isPinned(chatId, messageId)).getBody();
    }
}
