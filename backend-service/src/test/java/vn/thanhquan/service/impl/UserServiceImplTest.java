package vn.thanhquan.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.repository.AddressRepository;
import vn.thanhquan.repository.UserRepository;
import vn.thanhquan.service.EmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Help to run test with Mockito
class UserServiceImplTest {

    @Mock // 1. Tạo một mock giả lập cho UserRepository
    private UserRepository userRepository;

    @Mock // Tạo mock cho các dependency khác
    private AddressRepository addressRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks // 2. Tiêm các mock đã tạo ở trên vào UserServiceImpl
    private UserServiceImpl userService;

    @Test
    void save_shouldCreateUserSuccessfully() throws Exception {
        // --- 1. Arrange (Chuẩn bị dữ liệu) ---
        UserCreationRequest request = new UserCreationRequest();
        // Giả sử request đã được setter các giá trị (username, password, email...)

        // Định nghĩa hành vi cho các mock
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");

        // Khi userRepository.save được gọi, trả về chính đối tượng đó và gán ID
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L); // Giả lập việc DB đã gán ID
            return user;
        });

        // --- 2. Act (Thực thi phương thức cần test) ---
        long userId = userService.save(request);

        // --- 3. Assert (Kiểm tra kết quả) ---
        assertEquals(1L, userId); // Kiểm tra ID trả về có đúng không

        // Sử dụng ArgumentCaptor để "bắt" đối tượng UserEntity được truyền vào hàm save
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture()); // Xác thực userRepository.save đã được gọi 1 lần

        UserEntity savedUser = userCaptor.getValue();
        assertEquals(request.getUsername(), savedUser.getUsername());
        assertEquals("encodedPassword123", savedUser.getPassword()); // Kiểm tra mật khẩu đã được mã hóa

        // Xác thực rằng phương thức gửi email đã được gọi đúng với email và username
        verify(emailService).emailVerification(request.getEmail(), request.getUsername());
    }
}