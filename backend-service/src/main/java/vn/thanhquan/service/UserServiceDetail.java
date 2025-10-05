package vn.thanhquan.service;

import java.util.ArrayList;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import vn.thanhquan.common.UserStatus;
import vn.thanhquan.model.UserEntity; // Model User của bạn
import vn.thanhquan.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceDetail implements UserDetailsService { // BẮT BUỘC implements UserDetailsService

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Dùng repository đã sửa ở Bước 1 để tìm user
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Kiểm tra trạng thái người dùng
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UsernameNotFoundException("User is inactive: " + username);
        }

        // 3. Chuyển đổi User model của bạn thành UserDetails của Spring Security
        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities() // Sử dụng quyền thực tế thay vì danh sách rỗng
        );
    }
}