package com.mstock.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mstock.api.payload.Request.RegisterRequest;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.services.UserService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/v0/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<GeneralResponde<?>> createUser(@RequestBody RegisterRequest request) {
        GeneralResponde<?> response = userService.createUser(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    

    @GetMapping
    public ResponseEntity<GeneralResponde<?>> getAllUsers() {
        GeneralResponde<?> response = userService.getAllUsers();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> getUser(@PathVariable Long id) {
        GeneralResponde<?> response = userService.getUser(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PutMapping
    public ResponseEntity<GeneralResponde<?>> updateUser(@RequestBody RegisterRequest request) {
        GeneralResponde<?> response = userService.updateUser(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> deleteUser(@PathVariable Long id) {
        GeneralResponde<?> response = userService.deleteUser(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
}
