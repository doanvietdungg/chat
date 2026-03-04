package chat.jace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test — kiểm tra Spring context load thành công.
 * Đặt tên kết thúc bằng IT để Surefire bỏ qua, chỉ Failsafe chạy.
 * Chạy thủ công: mvn failsafe:integration-test
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Load")
class JaceApplicationTests {

    @Test
    @DisplayName("contextLoads - Spring context khởi động thành công")
    void contextLoads() {
        // Kiểm tra toàn bộ context load không có exception
    }
}

