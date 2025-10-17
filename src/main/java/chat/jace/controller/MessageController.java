package chat.jace.controller;

import chat.jace.dto.message.MessageCreateRequest;
import chat.jace.dto.message.MessageResponse;
import chat.jace.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> list(@PathVariable UUID chatId, Pageable pageable) {
        return ResponseEntity.ok(messageService.list(chatId, pageable));
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<MessageResponse> send(@PathVariable UUID chatId, @Valid @RequestBody MessageCreateRequest request) {
        request.setChatId(chatId);
        return ResponseEntity.ok(messageService.send(request));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageResponse> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String text = body.get("text");
        return ResponseEntity.ok(messageService.update(id, text));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
