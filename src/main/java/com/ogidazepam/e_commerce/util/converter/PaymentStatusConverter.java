package com.ogidazepam.e_commerce.util.converter;

import com.ogidazepam.e_commerce.enums.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {
    @Override
    public String convertToDatabaseColumn(PaymentStatus paymentStatus) {
        return paymentStatus != null ? paymentStatus.getCode() : null;
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String s) {
        return s != null ? PaymentStatus.formCode(s) : null;
    }
}
