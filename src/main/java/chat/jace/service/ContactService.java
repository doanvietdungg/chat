package chat.jace.service;

import chat.jace.domain.Contact;
import chat.jace.domain.User;
import chat.jace.domain.UserPresence;
import chat.jace.domain.enums.PresenceStatus;
import chat.jace.dto.contact.ContactRequest;
import chat.jace.dto.contact.ContactResponse;
import chat.jace.repository.ContactRepository;
import chat.jace.repository.UserPresenceRepository;
import chat.jace.repository.UserRepository;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final UserPresenceRepository userPresenceRepository;

    @Transactional
    public ContactResponse addContact(ContactRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        // Validate contact user exists
        User contactUser = userRepository.findById(request.getContactUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if already a contact
        if (contactRepository.existsByUserIdAndContactUserId(currentUserId, request.getContactUserId())) {
            throw new IllegalArgumentException("User is already a contact");
        }
        
        // Cannot add self as contact
        if (currentUserId.equals(request.getContactUserId())) {
            throw new IllegalArgumentException("Cannot add yourself as contact");
        }
        
        Contact contact = Contact.builder()
                .userId(currentUserId)
                .contactUserId(request.getContactUserId())
                .displayName(request.getDisplayName())
                .build();
        
        contact = contactRepository.save(contact);
        
        return buildContactResponse(contact, contactUser, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> getContacts(Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Page<Contact> contacts = contactRepository.findByUserIdOrderByUpdatedAtDesc(currentUserId, pageable);
        
        if (contacts.isEmpty()) {
            return contacts.map(contact -> null); // Empty page
        }
        
        List<UUID> contactUserIds = contacts.getContent().stream()
                .map(Contact::getContactUserId)
                .collect(Collectors.toList());
        
        // Get contact users info
        List<User> contactUsers = userRepository.findByIdInOrderByUsername(contactUserIds);
        Map<UUID, User> userMap = contactUsers.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // Get presence info
        List<UserPresence> presences = userPresenceRepository.findByUserIdIn(contactUserIds);
        Map<UUID, UserPresence> presenceMap = presences.stream()
                .collect(Collectors.toMap(UserPresence::getUserId, Function.identity()));
        
        // Get mutual contacts count
        Map<UUID, Integer> mutualContactsMap = contactUserIds.stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    userId -> contactRepository.countMutualContacts(currentUserId, userId)
                ));
        
        return contacts.map(contact -> {
            User contactUser = userMap.get(contact.getContactUserId());
            UserPresence presence = presenceMap.get(contact.getContactUserId());
            Integer mutualContactsCount = mutualContactsMap.get(contact.getContactUserId());
            
            return ContactResponse.builder()
                    .id(contact.getId())
                    .userId(contact.getUserId())
                    .contactUserId(contact.getContactUserId())
                    .displayName(contact.getDisplayName())
                    .username(contactUser != null ? contactUser.getUsername() : null)
                    .email(contactUser != null ? contactUser.getEmail() : null)
                    .avatarUrl(contactUser != null ? contactUser.getAvatarUrl() : null)
                    .presenceStatus(presence != null ? presence.getStatus() : PresenceStatus.OFFLINE)
                    .lastSeenAt(presence != null ? presence.getLastSeenAt() : null)
                    .mutualContactsCount(mutualContactsCount != null ? mutualContactsCount : 0)
                    .createdAt(contact.getCreatedAt())
                    .updatedAt(contact.getUpdatedAt())
                    .build();
        });
    }

    @Transactional
    public ContactResponse updateContact(UUID contactUserId, ContactRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Contact contact = contactRepository.findByUserIdAndContactUserId(currentUserId, contactUserId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        
        contact.setDisplayName(request.getDisplayName());
        contact.setUpdatedAt(OffsetDateTime.now());
        
        contact = contactRepository.save(contact);
        
        User contactUser = userRepository.findById(contactUserId)
                .orElseThrow(() -> new IllegalArgumentException("Contact user not found"));
        
        return buildContactResponse(contact, contactUser, currentUserId);
    }

    @Transactional
    public void removeContact(UUID contactUserId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        if (!contactRepository.existsByUserIdAndContactUserId(currentUserId, contactUserId)) {
            throw new IllegalArgumentException("Contact not found");
        }
        
        contactRepository.deleteByUserIdAndContactUserId(currentUserId, contactUserId);
    }

    @Transactional(readOnly = true)
    public ContactResponse getContact(UUID contactUserId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Contact contact = contactRepository.findByUserIdAndContactUserId(currentUserId, contactUserId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        
        User contactUser = userRepository.findById(contactUserId)
                .orElseThrow(() -> new IllegalArgumentException("Contact user not found"));
        
        return buildContactResponse(contact, contactUser, currentUserId);
    }

    private ContactResponse buildContactResponse(Contact contact, User contactUser, UUID currentUserId) {
        // Get presence info
        UserPresence presence = userPresenceRepository.findByUserId(contact.getContactUserId())
                .orElse(null);
        
        // Get mutual contacts count
        Integer mutualContactsCount = contactRepository.countMutualContacts(currentUserId, contact.getContactUserId());
        
        return ContactResponse.builder()
                .id(contact.getId())
                .userId(contact.getUserId())
                .contactUserId(contact.getContactUserId())
                .displayName(contact.getDisplayName())
                .username(contactUser.getUsername())
                .email(contactUser.getEmail())
                .avatarUrl(contactUser.getAvatarUrl())
                .presenceStatus(presence != null ? presence.getStatus() : PresenceStatus.OFFLINE)
                .lastSeenAt(presence != null ? presence.getLastSeenAt() : null)
                .mutualContactsCount(mutualContactsCount != null ? mutualContactsCount : 0)
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}