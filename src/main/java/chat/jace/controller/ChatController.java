package chat.jace.controller;

import chat.jace.dto.chat.ChatCreateRequest;
import chat.jace.dto.chat.ChatResponse;
import chat.jace.dto.chat.ChatUpdateRequest;
import chat.jace.dto.common.ResponseFactory;
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
    public ResponseEntity<?> list(Pageable pageable) {
        Page<ChatResponse> chats = chatService.listMyChats(pageable);
        return ResponseFactory.success(chats, "Lấy danh sách chat thành công");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ChatCreateRequest request) {
        ChatResponse chat = chatService.create(request);
        return ResponseFactory.created(chat, "Tạo chat thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") UUID id) {
        ChatResponse chat = chatService.get(id);
        return ResponseFactory.success(chat, "Lấy thông tin chat thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody ChatUpdateRequest request) {
        ChatResponse chat = chatService.update(id, request);
        return ResponseFactory.success(chat, "Cập nhật chat thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrLeave(@PathVariable("id") UUID id) {
        chatService.deleteOrLeave(id);
        return ResponseFactory.noContent("Xóa/Thoát chat thành công");
    }
}
