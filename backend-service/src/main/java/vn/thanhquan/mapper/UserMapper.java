
package vn.thanhquan.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.controller.response.UserResponse;
import vn.thanhquan.model.AddressEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "gender", target = "gender") // Map enum sang String
    @Mapping(source = "addresses", target = "addresses")
    UserResponse toUserResponse(UserEntity userEntity);

    List<UserResponse> toUserResponseList(List<UserEntity> userEntities);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "apartmentNumber", target = "apartmentNumber")
    @Mapping(source = "floor", target = "floor")
    @Mapping(source = "building", target = "building")
    @Mapping(source = "streetNumber", target = "streetNumber")
    @Mapping(source = "street", target = "street")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "addressType", target = "addressType")
    UserResponse.AddressResponse toAddressResponse(AddressEntity addressEntity);
}