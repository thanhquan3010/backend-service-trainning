package vn.thanhquan.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails.Address;
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
import vn.thanhquan.model.AddressEntity;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.controller.request.AddressRequest;
import vn.thanhquan.repository.AddressRepository;
import vn.thanhquan.repository.UserRepository;
import vn.thanhquan.service.EmailService;
import vn.thanhquan.service.UserService;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final AddressRequest addressRequest;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public List<UserResponse> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public UserResponse findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public UserResponse findByUsername(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByUsername'");
    }

    @Override
    public UserResponse findByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByEmail'");
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
    @Transactional // Thêm @Transactional để đảm bảo tất cả các thao tác đều thành công hoặc thất
                   // bại cùng nhau
    public void update(UserUpdateRequest req) {
        log.info("Updating user: {}", req);

        // 1. Lấy user hiện tại từ DB
        UserEntity user = userRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + req.getId()));

        // 2. Cập nhật thông tin cho user
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());

        // 3. Lưu user đã cập nhật và lấy về đối tượng đã được quản lý bởi JPA
        UserEntity savedUser = userRepository.save(user);
        log.info("Updated user successfully: {}", savedUser.getId());

        // 4. Cập nhật hoặc thêm mới địa chỉ
        if (req.getAddressRequests() != null) {
            req.getAddressRequests().forEach(addressRequest -> {

                // Tìm địa chỉ dựa trên user và loại địa chỉ
                AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(
                        savedUser.getId(),
                        addressRequest.getAddressType());

                // Nếu chưa có -> tạo mới
                if (addressEntity == null) {
                    addressEntity = new AddressEntity();
                    // Gán user cho địa chỉ mới
                    addressEntity.setUser(savedUser);
                }

                // Cập nhật thông tin từ request
                addressEntity.setApartmentNumber(addressRequest.getApartmentNumber());
                addressEntity.setFloor(addressRequest.getFloor());
                addressEntity.setBuilding(addressRequest.getBuilding());
                addressEntity.setStreetNumber(addressRequest.getStreetNumber());
                addressEntity.setStreet(addressRequest.getStreet());
                addressEntity.setCity(addressRequest.getCity());
                addressEntity.setCountry(addressRequest.getCountry());
                addressEntity.setAddressType(addressRequest.getAddressType());

                // Lưu địa chỉ vào DB
                addressRepository.save(addressEntity);
            });
        }
    }

    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Changin password for user: {}", req);
        // get user entity
        UserEntity userEntity = getUserEntity(req.getId());
        if (req.getPassword().equals(req.getConfirmPassword())) {
            userEntity.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(userEntity);
        log.info("Changing password success{}", userEntity);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user: {}", id);

        // Get user by id
        UserEntity user = getUserEntity(id); // Giả định đây là một phương thức private
        user.setStatus(UserStatus.INACTIVE);

        userRepository.save(user);
        log.info("Deleted user: {}", user);
    }

    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
