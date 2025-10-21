package vn.thanhquan.service;

import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.controller.request.UserPasswordRequest;
import vn.thanhquan.controller.request.UserUpdateRequest;
import vn.thanhquan.controller.response.UserResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    List<UserResponse> findAll();
    
    Page<UserResponse> findAllWithPagination(Pageable pageable);
    
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    long save(UserCreationRequest req);

    void update(UserUpdateRequest req);

    void changePassword(UserPasswordRequest req);

    void delete(Long id);

    // Add method to confirm email
    void confirmEmail(String secretCode);

}
