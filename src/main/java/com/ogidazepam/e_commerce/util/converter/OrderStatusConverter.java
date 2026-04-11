package com.ogidazepam.e_commerce.util.converter;

import com.ogidazepam.e_commerce.enums.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus orderStatus) {
        return orderStatus != null ? orderStatus.getCode() : null;
    }

    @Override
    public OrderStatus convertToEntityAttribute(String s) {
        return s != null ? OrderStatus.formCode(s) : null;
    }
}
