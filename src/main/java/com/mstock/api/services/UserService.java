package com.mstock.api.services;

import java.util.List;

import com.mstock.api.DTO.UserDTO;
import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;

public interface UserService {
    GeneralResponde<?> createUser(RegisterRequest UserRequest);
    GeneralResponde<List<UserDTO>> getAllUsers();
	GeneralResponde<?> getUser(Long id);
    GeneralResponde<?> updateUser(RegisterRequest UserRequest);
    GeneralResponde<?> deleteUser(Long id);
}
