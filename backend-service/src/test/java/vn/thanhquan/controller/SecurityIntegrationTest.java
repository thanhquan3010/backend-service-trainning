package vn.thanhquan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.model.Role;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.model.UserHasRole;
import vn.thanhquan.repository.RoleRepository;
import vn.thanhquan.repository.UserRepository;
import vn.thanhquan.service.JwtService;

import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Đảm bảo các thay đổi trong test được rollback sau khi hoàn tất
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Giả sử bạn có RoleRepository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private UserEntity testAdmin;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // --- Tạo Roles ---
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        // --- Tạo User có quyền ADMIN ---
        testAdmin = new UserEntity();
        testAdmin.setUsername("testadmin");
        testAdmin.setPassword(passwordEncoder.encode("password123"));
        testAdmin.setEmail("testadmin@example.com");
        testAdmin.setFirstName("Admin");
        userRepository.save(testAdmin);

        UserHasRole adminHasRole = new UserHasRole();
        adminHasRole.setUser(testAdmin);
        adminHasRole.setRole(adminRole);
        testAdmin.setRoles(List.of(adminHasRole));
        userRepository.save(testAdmin);

        adminToken = jwtService.generateAccessToken(testAdmin.getId(), testAdmin.getUsername(),
                testAdmin.getAuthorities());

        // --- Tạo User chỉ có quyền USER ---
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("testuser@example.com");
        testUser.setFirstName("User");
        userRepository.save(testUser);

        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(testUser);
        userHasRole.setRole(userRole);
        testUser.setRoles(List.of(userHasRole));
        userRepository.save(testUser);

        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getUsername(), testUser.getAuthorities());
    }

    // ==================================================
    // 1. KIỂM THỬ PHÂN QUYỀN (AUTHORIZATION)
    // ==================================================

    @Test
    void whenNoToken_thenAccessProtectedEndpoint_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUserRole_thenAccessAdminEndpoint_shouldReturn403Forbidden() throws Exception {
        // Giả sử endpoint DELETE /user/{userId}/del yêu cầu quyền ADMIN
        mockMvc.perform(delete("/user/{userId}/del", testUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ==================================================
    // 2. KIỂM THỬ XÁC THỰC (AUTHENTICATION)
    // ==================================================

    @Test
    void whenExpiredToken_thenAccessProtectedEndpoint_shouldReturn401Unauthorized() throws Exception {
        // Tạo một token hết hạn (sử dụng key giả lập vì key thật không được truy cập)
        String expiredToken = Jwts.builder()
                .subject("expireduser")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000)) // Đã hết hạn
                .signWith(Keys.hmacShaKeyFor("9NR82kt/j0MxbBcQuHDFUINBrF9a1D2dLVbO2dEXtXc=".getBytes())) // Sử dụng key
                                                                                                         // test
                .compact();

        mockMvc.perform(get("/user/list")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenInvalidSignatureToken_thenAccessProtectedEndpoint_shouldReturn401Unauthorized() throws Exception {
        // Sửa đổi token để chữ ký không hợp lệ
        String invalidSignatureToken = adminToken + "tampered";

        mockMvc.perform(get("/user/list")
                .header("Authorization", "Bearer " + invalidSignatureToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenTokenIsNotBearer_thenFilterShouldIgnore_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/user/list")
                .header("Authorization", "Basic " + adminToken)) // Dùng "Basic" thay vì "Bearer"
                .andExpect(status().isUnauthorized());
    }

    // ==================================================
    // 3. KIỂM THỬ XÁC THỰC ĐẦU VÀO (INPUT VALIDATION)
    // ==================================================

    @Test
    void whenCreateUserWithInvalidEmail_shouldReturn400BadRequest() throws Exception {
        UserCreationRequest request = new UserCreationRequest();
        // Gán các giá trị hợp lệ cho các trường khác
        // Setter cho các trường của request không có sẵn, cần được thêm vào class
        // UserCreationRequest
        // Giả sử có các setter:
        // request.setFirstName("Test");
        // request.setUsername("newuser123");
        // request.setPassword("password123");
        // request.setEmail("invalid-email");

        // Vì không có setters, ta sẽ tạo JSON thủ công
        String userJson = "{\"firstName\":\"Test\", \"username\":\"newuser123\", \"password\":\"password123\", \"email\":\"invalid-email\"}";

        mockMvc.perform(post("/user/add")
                .header("Authorization", "Bearer " + adminToken) // Cần token để qua cửa xác thực
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateUserWithShortPassword_shouldReturn400BadRequest() throws Exception {
        // Password yêu cầu tối thiểu 6 ký tự
        String userJson = "{\"firstName\":\"Test\", \"username\":\"newuser456\", \"password\":\"12345\", \"email\":\"valid@email.com\"}";

        mockMvc.perform(post("/user/add")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }
}