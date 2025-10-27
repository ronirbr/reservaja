package com.reservaja.dto.auth;

import lombok.Data;
import com.reservaja.model.entity.User;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    // opcional: role (somente para criação por admin)
    private User.Role  role;
}
