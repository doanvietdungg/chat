package chat.jace.controller;

import chat.jace.dto.common.ResponseFactory;
import chat.jace.dto.user.UserSearchRequest;
import chat.jace.dto.user.UserSearchResponse;
import chat.jace.service.UserSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    @PostMapping("/search")
    public ResponseEntity<?> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        List<UserSearchResponse> users = userSearchService.searchUsers(request);
        return ResponseFactory.success(users, "Tìm kiếm người dùng thành công");
    }

    @GetMapping("/search/recent")
    public ResponseEntity<?> getRecentSearches() {
        List<UserSearchResponse> users = userSearchService.getRecentSearches();
        return ResponseFactory.success(users, "Lấy lịch sử tìm kiếm thành công");
    }

    @GetMapping("/search/suggested")
    public ResponseEntity<?> getSuggestedContacts() {
        List<UserSearchResponse> users = userSearchService.getSuggestedContacts();
        return ResponseFactory.success(users, "Lấy gợi ý liên hệ thành công");
    }

    @PostMapping("/search/history/{userId}")
    public ResponseEntity<?> saveSearchHistory(@PathVariable UUID userId) {
        userSearchService.saveSearchHistory(userId);
        return ResponseFactory.success(null, "Lưu lịch sử tìm kiếm thành công");
    }

    @DeleteMapping("/search/history")
    public ResponseEntity<?> clearSearchHistory() {
        userSearchService.clearSearchHistory();
        return ResponseFactory.success(null, "Xóa lịch sử tìm kiếm thành công");
    }
}