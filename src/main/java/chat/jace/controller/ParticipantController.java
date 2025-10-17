package chat.jace.controller;

import chat.jace.dto.participant.ParticipantAddRequest;
import chat.jace.dto.participant.ParticipantResponse;
import chat.jace.dto.participant.ParticipantUpdateRequest;
import chat.jace.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats/{chatId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> list(@PathVariable UUID chatId) {
        return ResponseEntity.ok(participantService.list(chatId));
    }

    @PostMapping
    public ResponseEntity<ParticipantResponse> add(@PathVariable UUID chatId,
                                                   @Valid @RequestBody ParticipantAddRequest request) {
        return ResponseEntity.ok(participantService.add(chatId, request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ParticipantResponse> updateRole(@PathVariable UUID chatId,
                                                          @PathVariable UUID userId,
                                                          @Valid @RequestBody ParticipantUpdateRequest request) {
        return ResponseEntity.ok(participantService.updateRole(chatId, userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable UUID chatId, @PathVariable UUID userId) {
        participantService.remove(chatId, userId);
        return ResponseEntity.noContent().build();
    }
}
