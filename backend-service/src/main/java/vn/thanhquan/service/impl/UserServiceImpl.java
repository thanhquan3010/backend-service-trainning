package vn.thanhquan.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.thanhquan.common.UserStatus;
import vn.thanhquan.controller.request.UserCreationRequest;
import vn.thanhquan.controller.request.UserPasswordRequest;
import vn.thanhquan.controller.request.UserUpdateRequest;
import vn.thanhquan.controller.response.UserResponse;
import vn.thanhquan.exception.ResourceNotFoundException;
import vn.thanhquan.mapper.UserMapper;
import vn.thanhquan.model.AddressEntity;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.repository.AddressRepository;
import vn.thanhquan.repository.UserRepository;
import vn.thanhquan.service.EmailService;
import vn.thanhquan.service.UserService;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> findAll() {
        log.info("Finding all users");
        List<UserEntity> users = userRepository.findAll();
        // Dùng mapper để chuyển đổi
        return userMapper.toUserResponseList(users);
    }

    @Override
    public UserResponse findById(Long id) {
        log.info("Finding user by id: {}", id);
        UserEntity userEntity = getUserEntity(id);
        // Dùng mapper để chuyển đổi
        return userMapper.toUserResponse(userEntity);
    }

    @Override
    public UserResponse findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toUserResponse(userEntity);
    }

    @Override
    public UserResponse findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(userEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // set up transaction
    public long save(UserCreationRequest req) {
        log.info("Saving user: {}", req);
        UserEntity user = new UserEntity();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        user.setType(req.getType());
        user.setStatus(UserStatus.NONE);
        // Note: Password is not set here, which might be an issue for login
        // --- Bổ sung mã hóa mật khẩu ---
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        } else {
            // Bạn nên có xử lý cho trường hợp mật khẩu rỗng, ví dụ ném ra exception
            throw new IllegalArgumentException("Password cannot be empty");
        }
        UserEntity savedUser = userRepository.save(user);

        if (req.getAddresses() != null && !req.getAddresses().isEmpty()) {
            List<AddressEntity> addresses = new ArrayList<>();
            req.getAddresses().forEach(address -> {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setFloor(address.getFloor());
                addressEntity.setBuilding(address.getBuilding());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setStreet(address.getStreet());
                addressEntity.setCity(address.getCity());
                addressEntity.setCountry(address.getCountry());
                addressEntity.setAddressType(address.getAddressType());
                addressEntity.setUser(savedUser);
                addressEntity.setUserId(savedUser.getId());
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Saved addresses: {}", addresses);
        }

        // send email
        try {
            emailService.emailVerification(req.getEmail(), req.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return savedUser.getId();
    }

    @Override
    @Transactional
    public void update(UserUpdateRequest req) {
        log.info("Updating user: {}", req);
        UserEntity user = getUserEntity(req.getId());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        UserEntity savedUser = userRepository.save(user);
        log.info("Updated user successfully: {}", savedUser.getId());

        if (req.getAddressRequests() != null) {
            req.getAddressRequests().forEach(addressRequest -> {
                AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(
                        savedUser.getId(),
                        addressRequest.getAddressType());
                if (addressEntity == null) {
                    addressEntity = new AddressEntity();
                    addressEntity.setUser(savedUser);
                }
                addressEntity.setApartmentNumber(addressRequest.getApartmentNumber());
                addressEntity.setFloor(addressRequest.getFloor());
                addressEntity.setBuilding(addressRequest.getBuilding());
                addressEntity.setStreetNumber(addressRequest.getStreetNumber());
                addressEntity.setStreet(addressRequest.getStreet());
                addressEntity.setCity(addressRequest.getCity());
                addressEntity.setCountry(addressRequest.getCountry());
                addressEntity.setAddressType(addressRequest.getAddressType());
                addressRepository.save(addressEntity);
            });
        }
    }

    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Changing password for user: {}", req.getId());
        UserEntity userEntity = getUserEntity(req.getId());
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        userEntity.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(userEntity);
        log.info("Password changed successfully for user {}", userEntity.getId());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user: {}", id);
        UserEntity user = getUserEntity(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Deactivated user: {}", user.getId());
    }

    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Add method to confirm email
    @Override
    public void confirmEmail(String secretCode) {
        log.info("Confirming email with secret code: {}", secretCode);
        UserEntity user = userRepository.findByVerificationCode(secretCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification code: " + secretCode));

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null); // Xóa mã sau khi sử dụng
        userRepository.save(user);
        log.info("User {}'s email confirmed successfully", user.getUsername());
    }

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