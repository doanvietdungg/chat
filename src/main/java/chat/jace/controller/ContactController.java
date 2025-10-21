package chat.jace.controller;

import chat.jace.dto.common.ResponseFactory;
import chat.jace.dto.contact.ContactRequest;
import chat.jace.dto.contact.ContactResponse;
import chat.jace.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> addContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse contact = contactService.addContact(request);
        return ResponseFactory.created(contact, "Thêm liên hệ thành công");
    }

    @GetMapping
    public ResponseEntity<?> getContacts(@PageableDefault(size = 20) Pageable pageable) {
        Page<ContactResponse> contacts = contactService.getContacts(pageable);
        return ResponseFactory.success(contacts, "Lấy danh sách liên hệ thành công");
    }

    @GetMapping("/{contactUserId}")
    public ResponseEntity<?> getContact(@PathVariable UUID contactUserId) {
        ContactResponse contact = contactService.getContact(contactUserId);
        return ResponseFactory.success(contact, "Lấy thông tin liên hệ thành công");
    }

    @PutMapping("/{contactUserId}")
    public ResponseEntity<?> updateContact(@PathVariable UUID contactUserId, 
                                         @Valid @RequestBody ContactRequest request) {
        ContactResponse contact = contactService.updateContact(contactUserId, request);
        return ResponseFactory.success(contact, "Cập nhật liên hệ thành công");
    }

    @DeleteMapping("/{contactUserId}")
    public ResponseEntity<?> removeContact(@PathVariable UUID contactUserId) {
        contactService.removeContact(contactUserId);
        return ResponseFactory.success(null, "Xóa liên hệ thành công");
    }
}