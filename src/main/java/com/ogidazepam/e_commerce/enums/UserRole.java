package com.ogidazepam.e_commerce.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    USER("U"),
    ADMIN("A");

    private final String code;

    UserRole(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public SimpleGrantedAuthority toAuthority(){
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }

    public static UserRole formCode(String code){
        for (UserRole userRole : values()){
            if(userRole.getCode().equals(code)){
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
