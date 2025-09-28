package vn.thanhquan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.thanhquan.model.AddressEntity;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    AddressEntity findByUserIdAndAddressType(Long userId, Integer addressType);
}
