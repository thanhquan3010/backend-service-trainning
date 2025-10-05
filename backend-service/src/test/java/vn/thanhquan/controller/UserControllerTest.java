package vn.thanhquan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // 1. Chỉ khởi tạo môi trường test cho UserController
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // 2. Đối tượng để thực hiện các cuộc gọi HTTP ảo

    @Autowired
    private ObjectMapper objectMapper; // Dùng để chuyển đổi object Java sang JSON

    @Mock // 3. Tạo một mock cho UserService và đưa vào ApplicationContext
    private UserService userService;

    @Test
    void createUser_whenValidRequest_shouldReturnCreated() throws Exception {
        // --- 1. Arrange ---
        UserCreationRequest request = new UserCreationRequest();
        // Giả sử request đã được setter các giá trị

        // Định nghĩa hành vi cho mock service
        when(userService.save(any(UserCreationRequest.class))).thenReturn(1L);

        // --- 2. Act & 3. Assert ---
        mockMvc.perform(post("/user/add") // Thực hiện request POST
                .contentType(MediaType.APPLICATION_JSON) // Set content type
                .content(objectMapper.writeValueAsString(request))) // Set body của request
                .andExpect(status().isCreated()) // Mong đợi HTTP Status 201 Created
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data").value(1L)); // Kiểm tra nội dung JSON trả về
    }
}