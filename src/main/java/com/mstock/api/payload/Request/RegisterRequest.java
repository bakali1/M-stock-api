package com.mstock.api.payload.Request;

import com.mstock.api.Enum.UserRoleEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    private Long id;
    private String username;
    private String password;
    private String email;
    private UserRoleEnum role;
    @Default
    private Boolean active = true;
}
