package com.ogidazepam.e_commerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerLoginDTO {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
