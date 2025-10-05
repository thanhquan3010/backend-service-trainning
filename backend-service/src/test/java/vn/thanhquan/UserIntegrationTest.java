package vn.thanhquan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // 1. Tải toàn bộ ApplicationContext
@AutoConfigureMockMvc // 2. Tự động cấu hình MockMvc
@Transactional // 3. Đảm bảo transaction được rollback sau mỗi test, giữ cho DB sạch sẽ
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository; // Tiêm UserRepository thật để kiểm tra DB

    @Test
    void createUser_shouldSaveUserInDatabase() throws Exception {
        // --- 1. Arrange ---
        UserCreationRequest request = new UserCreationRequest();
        // Giả sử request đã được setter các giá trị (username="integration_user", ...)

        // --- 2. Act ---
        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").isNumber());

        // --- 3. Assert ---
        // Kiểm tra trực tiếp trong cơ sở dữ liệu H2
        Optional<UserEntity> foundUser = userRepository.findByUsername(request.getUsername());
        assertTrue(foundUser.isPresent(), "User nên được tìm thấy trong database");
        assertEquals(request.getEmail(), foundUser.get().getEmail());
        assertNotNull(foundUser.get().getPassword()); // Mật khẩu không nên là null
    }
}