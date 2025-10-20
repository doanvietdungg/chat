package chat.jace.dto.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public class ResponseFactory {
    
    // Success responses
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Tạo thành công"));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.<T>builder()
                        .success(true)
                        .message("Xóa thành công")
                        .build());
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> noContent(String message) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .build());
    }
    
    // Error responses
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message, Object errors) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, errors));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message, Object errors) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message, errors));
    }
    
    // Validation error response
    public static <T> ResponseEntity<ApiResponse<T>> validationError(String message, Map<String, List<String>> fieldErrors) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.<T>builder()
                        .success(false)
                        .message(message)
                        .errors(fieldErrors)
                        .code(400)
                        .build());
    }
    
    // Custom status response
    public static <T> ResponseEntity<ApiResponse<T>> custom(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message));
    }
    
    public static <T> ResponseEntity<ApiResponse<T>> custom(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .success(status.is2xxSuccessful())
                        .message(message)
                        .data(data)
                        .code(status.value())
                        .build());
    }
}
