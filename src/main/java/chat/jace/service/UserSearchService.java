package chat.jace.service;

import chat.jace.domain.Contact;
import chat.jace.domain.SearchHistory;
import chat.jace.domain.User;
import chat.jace.domain.UserPresence;
import chat.jace.domain.enums.PresenceStatus;
import chat.jace.dto.user.UserSearchRequest;
import chat.jace.dto.user.UserSearchResponse;
import chat.jace.repository.*;
import chat.jace.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
public class UserSearchService {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserPresenceRepository userPresenceRepository;

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(UserSearchRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Pageable pageable = PageRequest.of(0, Math.min(request.getLimit(), 50));
        List<User> users = userRepository.searchByUsernameOrEmail(request.getQuery(), pageable);
        
        // Filter out current user
        users = users.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
        
        return buildUserSearchResponses(users, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> getRecentSearches() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Pageable pageable = PageRequest.of(0, 10);
        List<UUID> recentUserIds = searchHistoryRepository.findRecentSearchedUserIds(currentUserId, pageable);
        
        if (recentUserIds.isEmpty()) {
            return List.of();
        }
        
        List<User> users = userRepository.findByIdInOrderByUsername(recentUserIds);
        return buildUserSearchResponses(users, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> getSuggestedContacts() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        Pageable pageable = PageRequest.of(0, 20);
        List<User> suggestedUsers = userRepository.findSuggestedContacts(currentUserId, pageable);
        
        return buildUserSearchResponses(suggestedUsers, currentUserId);
    }

    @Transactional
    public void saveSearchHistory(UUID searchedUserId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        // Don't save search history for self
        if (currentUserId.equals(searchedUserId)) {
            return;
        }
        
        // Remove existing entry if exists
        if (searchHistoryRepository.existsByUserIdAndSearchedUserId(currentUserId, searchedUserId)) {
            searchHistoryRepository.deleteByUserIdAndSearchedUserId(currentUserId, searchedUserId);
        }
        
        // Add new entry
        SearchHistory searchHistory = SearchHistory.builder()
                .userId(currentUserId)
                .searchedUserId(searchedUserId)
                .build();
        
        searchHistoryRepository.save(searchHistory);
    }

    @Transactional
    public void clearSearchHistory() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        
        // Delete search history older than 30 days
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        searchHistoryRepository.deleteByUserIdAndSearchedAtBefore(currentUserId, thirtyDaysAgo);
    }

    private List<UserSearchResponse> buildUserSearchResponses(List<User> users, UUID currentUserId) {
        if (users.isEmpty()) {
            return List.of();
        }
        
        List<UUID> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        
        // Get contacts info
        List<Contact> contacts = contactRepository.findByUserIdOrderByUpdatedAtDesc(currentUserId);
        Map<UUID, Contact> contactMap = contacts.stream()
                .collect(Collectors.toMap(Contact::getContactUserId, Function.identity()));
        
        // Get presence info
        List<UserPresence> presences = userPresenceRepository.findByUserIdIn(userIds);
        Map<UUID, UserPresence> presenceMap = presences.stream()
                .collect(Collectors.toMap(UserPresence::getUserId, Function.identity()));
        
        // Get mutual contacts count
        Map<UUID, Integer> mutualContactsMap = userIds.stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    userId -> contactRepository.countMutualContacts(currentUserId, userId)
                ));
        
        return users.stream()
                .map(user -> {
                    Contact contact = contactMap.get(user.getId());
                    UserPresence presence = presenceMap.get(user.getId());
                    Integer mutualContactsCount = mutualContactsMap.get(user.getId());
                    
                    return UserSearchResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .avatarUrl(user.getAvatarUrl())
                            .presenceStatus(presence != null ? presence.getStatus() : PresenceStatus.OFFLINE)
                            .lastSeenAt(presence != null ? presence.getLastSeenAt() : null)
                            .isContact(contact != null)
                            .mutualContactsCount(mutualContactsCount != null ? mutualContactsCount : 0)
                            .displayName(contact != null ? contact.getDisplayName() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}