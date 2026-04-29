package com.mstock.api.Mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.mstock.api.DTO.UserDTO;
import com.mstock.api.entities.User;
import com.mstock.api.payload.Request.RegisterRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeforeMapping
    default void normalizeRequest(User user){
        if(user.getUsername() != null && user.getUsername().trim().isEmpty()){
            user.setUsername(null);
        }
        if(user.getEmail() != null && user.getEmail().trim().isEmpty()){
            user.setEmail(null);
        }
        if(user.getPassword() != null && user.getPassword().trim().isEmpty()){
            user.setUsername(null);
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "auditLogs", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    void updateUserFromRequest(RegisterRequest request,@MappingTarget User user);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    UserDTO toUserDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    List<UserDTO> toUserDTO(List<User> users);
}
