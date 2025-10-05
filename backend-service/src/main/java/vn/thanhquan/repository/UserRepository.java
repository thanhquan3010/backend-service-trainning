package vn.thanhquan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.thanhquan.model.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Add method to find user by username
    Optional<UserEntity> findByUsername(String username);

    // Add method to find user by email
    Optional<UserEntity> findByVerificationCode(String verificationCode);
}
