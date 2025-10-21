package vn.thanhquan.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.controller.request.UserPasswordRequest;
import vn.thanhquan.controller.request.UserUpdateRequest;
import vn.thanhquan.controller.response.UserResponse;
import vn.thanhquan.service.UserService;

@RestController
@RequestMapping("/user")
@Tag(name = "UserController")
@Slf4j(topic = "USER-CONTROLLER")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user list", description = "API retrieve user from db with pagination and search")
    @GetMapping("/list")
    public Map<String, Object> getList(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int size, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Create Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Search with keyword
            userPage = userService.searchUsers(keyword.trim(), pageable);
        } else {
            // Get all users with pagination
            userPage = userService.findAllWithPagination(pageable);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user list");
        result.put("data", userPage.getContent());
        result.put("pagination", Map.of(
            "currentPage", userPage.getNumber(),
            "totalPages", userPage.getTotalPages(),
            "totalElements", userPage.getTotalElements(),
            "size", userPage.getSize(),
            "hasNext", userPage.hasNext(),
            "hasPrevious", userPage.hasPrevious()
        ));

        return result;
    }

    @Operation(summary = "Get user detail", description = "API retrieve user detail from db")
    @GetMapping("/{userId}")
    public Map<String, Object> getUserDetail(@PathVariable Long userId) {
        // Replace mock data with service call
        UserResponse userDetail = userService.findById(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user detail");
        result.put("data", userDetail);
        return result;
    }

    @Operation(summary = "Create User", description = "API add new user to db")
    @PostMapping("/add")
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreationRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "User created successfully");
        result.put("data", userService.save(request));

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update User", description = "API update user to db")
    @PutMapping("/update")
    public Map<String, Object> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        // Call the service to perform the update
        userService.update(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "User updated successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Change Password", description = "API change password for user to database")
    @PatchMapping("/change-pwd")
    public Map<String, Object> changePassword(@Valid @RequestBody UserPasswordRequest request) {
        // Call the service to change the password
        userService.changePassword(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.NO_CONTENT.value());
        result.put("message", "Password updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Inactivated user", description = "API activate user from database")
    @DeleteMapping("/{userId}/del")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> deleteUser(@PathVariable long userId) {
        // Call the service to delete (inactivate) the user
        userService.delete(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value()); // Using OK 200 or NO_CONTENT 204 is also common
        result.put("message", "User deleted successfully");
        result.put("data", "");
        return result;
    }

    // Add endpoint to confirm email
    @Operation(summary = "Confirm Email", description = "API confirm email for user")
    @GetMapping("/confirm-email")
    public void confirmEmail(@RequestParam String secretCode, HttpServletResponse response) throws IOException {
        log.info("Confirm email request received with secretCode: {}", secretCode);
        try {
            userService.confirmEmail(secretCode);
            // Chuyển hướng đến trang thành công
            response.sendRedirect("https://tayjava.vn/xac-thuc-thanh-cong");
        } catch (Exception e) {
            log.error("Email confirmation failed: {}", e.getMessage());
            // Chuyển hướng đến trang lỗi
            response.sendRedirect("https://tayjava.vn/xac-thuc-that-bai");
        }
    }
}