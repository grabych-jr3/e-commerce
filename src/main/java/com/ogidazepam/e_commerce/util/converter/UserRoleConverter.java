package com.ogidazepam.e_commerce.util.converter;

import com.ogidazepam.e_commerce.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole userRole) {
        return userRole != null ? userRole.getCode() : null;
    }

    @Override
    public UserRole convertToEntityAttribute(String s) {
        return s != null ? UserRole.formCode(s) : null;
    }
}
