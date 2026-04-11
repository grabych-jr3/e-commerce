package com.ogidazepam.e_commerce.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {
    PENDING("PE"),
    ON_HOLD("OH"),
    PROCESSING("PR"),
    COMPLETED("CO"),
    FAILED("FA"),
    CANCELED("CA"),
    REFUNDED("RE");

    private final String code;

    public static OrderStatus formCode(String code){
        for(OrderStatus os : values()){
            if (os.getCode().equals(code)){
                return os;
            }
        }
        throw new IllegalArgumentException("Invalid order status code");
    }
}
