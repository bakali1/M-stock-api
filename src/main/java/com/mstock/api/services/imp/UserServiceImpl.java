package com.mstock.api.services.imp;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mstock.api.DTO.UserDTO;
import com.mstock.api.Mappers.UserMapper;
import com.mstock.api.entities.User;
import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.UserRepository;
import com.mstock.api.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public GeneralResponde<?> createUser(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            return GeneralResponde.<Void>builder()
                    .status(HttpStatus.CONFLICT.value())
                    .msg("user with this NSN code already exists")
                    .build();
        }

        User user = new User();
        userMapper.updateUserFromRequest(request, user);
        UserDTO saveduser = userMapper.toUserDTO(userRepository.save(user));

        return GeneralResponde.<UserDTO>builder()
                .status(HttpStatus.CREATED.value())
                .msg("user created successfully")
                .data(saveduser)
                .build();
    }
    public GeneralResponde<List<UserDTO>> getAllUsers(){
        return GeneralResponde.<List<UserDTO>>builder()
                        .status(200)
                        .data(userMapper.toUserDTO(userRepository.findByActiveTrue()))
                        .msg("All Users").build();
    }
	public GeneralResponde<?> getUser(Long id){
        User user = userRepository.findByIdAndActiveTrueWithoutRelations(id).orElse(null);
        if(user == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("user not found").build();
        }
        return GeneralResponde.<UserDTO>builder().data(userMapper.toUserDTO(user)).status(HttpStatus.OK.value()).msg("User was found").build();
    }
    public GeneralResponde<?> updateUser(RegisterRequest request){
        if(request.getId() == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .data(null).msg("id not provided").build();
        }
        User user = userRepository.findByIdWithoutRelations(request.getId()).orElse(null);
        if(user == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("user not found").build();
        }
        
        // Use the user's updateFromRequest method for partial updates
        userMapper.updateUserFromRequest(request, user);
        
        return GeneralResponde.<UserDTO>builder().data(userMapper.toUserDTO(userRepository.save(user))).msg("User updated successfully").status(HttpStatus.OK.value()).build();
    }
    public GeneralResponde<?> deleteUser(Long id){
        User user = userRepository.findByIdAndActiveTrueWithoutRelations(id).orElse(null); 
        if(user == null){
            return GeneralResponde.<Void>builder()
            .status(HttpStatus.NOT_FOUND.value())
            .data(null).msg("user not found").build();
        }
        user.setActive(false);
        userRepository.save(user);
        return GeneralResponde.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .msg("user deleted successfully")
                                .build(); 
    }
}

