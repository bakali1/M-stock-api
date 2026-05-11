package com.mstock.api.config.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mstock.api.entities.User;
import com.mstock.api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAndActiveTrue(username)
                .or(() -> userRepository.findByUsernameAndActiveTrue(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public UserDetails loadUserByEmail(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAndActiveTrue(username).orElse(null);
    }
}
