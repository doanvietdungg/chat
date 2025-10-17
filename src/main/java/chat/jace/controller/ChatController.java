package chat.jace.controller;

import chat.jace.dto.chat.ChatCreateRequest;
import chat.jace.dto.chat.ChatResponse;
import chat.jace.dto.chat.ChatUpdateRequest;
import chat.jace.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<Page<ChatResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(chatService.listMyChats(pageable));
    }

    @PostMapping
    public ResponseEntity<ChatResponse> create(@Valid @RequestBody ChatCreateRequest request) {
        return ResponseEntity.ok(chatService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(chatService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatResponse> update(@PathVariable("id") UUID id, @RequestBody ChatUpdateRequest request) {
        return ResponseEntity.ok(chatService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrLeave(@PathVariable("id") UUID id) {
        chatService.deleteOrLeave(id);
        return ResponseEntity.noContent().build();
    }
}
