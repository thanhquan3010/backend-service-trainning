
package vn.thanhquan.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.thanhquan.model.UserEntity;
import vn.thanhquan.controller.response.UserResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "gender", target = "gender") // Map enum sang String
    UserResponse toUserResponse(UserEntity userEntity);

    List<UserResponse> toUserResponseList(List<UserEntity> userEntities);
}