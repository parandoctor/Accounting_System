package com.accounting.system.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
