// src/main/java/vn/thanhquan/service/impl/AddressServiceImpl.java
package vn.thanhquan.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.thanhquan.controller.request.AddressRequest;
import vn.thanhquan.model.AddressEntity;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.repository.AddressRepository;
import vn.thanhquan.service.AddressService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public void saveAddresses(UserEntity user, List<AddressRequest> addresses) {
        if (addresses != null && !addresses.isEmpty()) {
            List<AddressEntity> addressEntities = new ArrayList<>();
            addresses.forEach(address -> {
                AddressEntity addressEntity = mapToAddressEntity(new AddressEntity(), address);
                addressEntity.setUser(user);
                addressEntity.setUserId(user.getId());
                addressEntities.add(addressEntity);
            });
            addressRepository.saveAll(addressEntities);
        }
    }

    @Override
    public void updateAddresses(UserEntity user, List<AddressRequest> addresses) {
        if (addresses != null) {
            addresses.forEach(addressRequest -> {
                AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(
                        user.getId(),
                        addressRequest.getAddressType());
                if (addressEntity == null) {
                    addressEntity = new AddressEntity();
                    addressEntity.setUser(user);
                }
                mapToAddressEntity(addressEntity, addressRequest);
                addressRepository.save(addressEntity);
            });
        }
    }

    private AddressEntity mapToAddressEntity(AddressEntity entity, AddressRequest request) {
        entity.setApartmentNumber(request.getApartmentNumber());
        entity.setFloor(request.getFloor());
        entity.setBuilding(request.getBuilding());
        entity.setStreetNumber(request.getStreetNumber());
        entity.setStreet(request.getStreet());
        entity.setCity(request.getCity());
        entity.setCountry(request.getCountry());
        entity.setAddressType(request.getAddressType());
        return entity;
    }
}