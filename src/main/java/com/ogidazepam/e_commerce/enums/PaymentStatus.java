package com.ogidazepam.e_commerce.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    CREATED("C"),
    SUCCESS("S"),
    FAILED("F");

    private final String code;

    public static PaymentStatus formCode(String code){
        for(PaymentStatus p : values()){
            if(p.getCode().equals(code)){
                return p;
            }
        }
        throw new IllegalArgumentException("Invalid code");
    }
}
