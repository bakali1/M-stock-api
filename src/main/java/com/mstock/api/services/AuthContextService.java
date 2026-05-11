package com.mstock.api.services;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mstock.api.entities.User;
import com.mstock.api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthContextService {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user);
        }

        if (principal instanceof UserDetails userDetails) {
            String identifier = userDetails.getUsername();
            return userRepository.findByEmailAndActiveTrue(identifier)
                    .or(() -> userRepository.findByUsernameAndActiveTrue(identifier));
        }

        if (principal instanceof String identifier) {
            return userRepository.findByEmailAndActiveTrue(identifier)
                    .or(() -> userRepository.findByUsernameAndActiveTrue(identifier));
        }

        return Optional.empty();
    }
}
