package fans.goldenglow.plumaspherebackend.mapper;

import fans.goldenglow.plumaspherebackend.dto.UserAdminDto;
import fans.goldenglow.plumaspherebackend.dto.UserDto;
import fans.goldenglow.plumaspherebackend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper extends BaseMapper {
    UserDto toDto(User user);

    List<UserDto> toDto(List<User> users);

    @Mapping(target = "isAdmin", expression = "java(user.getRole().equals(fans.goldenglow.plumaspherebackend.constant.UserRoles.ADMIN))")
    UserAdminDto toAdminDto(User user);

    List<UserAdminDto> toAdminDto(List<User> users);
}
