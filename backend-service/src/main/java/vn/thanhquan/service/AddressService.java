package vn.thanhquan.service;

import vn.thanhquan.controller.request.AddressRequest;
import vn.thanhquan.model.UserEntity;
import java.util.List;

public interface AddressService {
    void saveAddresses(UserEntity user, List<AddressRequest> addresses);

    void updateAddresses(UserEntity user, List<AddressRequest> addresses);
}