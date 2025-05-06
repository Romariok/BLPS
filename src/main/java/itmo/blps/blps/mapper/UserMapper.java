package itmo.blps.blps.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import itmo.blps.blps.dto.UserDTO;
import itmo.blps.blps.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    UserDTO userToUserDTO(User user);
}
