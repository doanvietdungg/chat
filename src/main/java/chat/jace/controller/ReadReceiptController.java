package chat.jace.controller;

import chat.jace.service.ReadReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReadReceiptController {

    private final ReadReceiptService readReceiptService;

    @PostMapping("/messages/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable UUID id) {
        readReceiptService.markRead(id);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
