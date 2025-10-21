package vn.thanhquan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.thanhquan.model.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Add method to find user by username
    Optional<UserEntity> findByUsername(String username);

    // Add method to find user by email
    Optional<UserEntity> findByVerificationCode(String verificationCode);

    Optional<UserEntity> findByEmail(String email);
    
    // Search users by keyword (firstName, lastName, email, username)
    @Query("SELECT u FROM UserEntity u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
