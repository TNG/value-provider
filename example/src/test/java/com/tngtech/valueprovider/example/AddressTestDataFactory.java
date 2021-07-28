package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.example.Address.AddressBuilder;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public final class AddressTestDataFactory {

    private AddressTestDataFactory() {
    }

    public static Address createAddress() {
        return createAddress(createRandomValueProvider());
    }

    public static Address createAddress(ValueProvider values) {
        return createAddressBuilder(values).build();
    }

    public static AddressBuilder createAddressBuilder() {
        return createAddressBuilder(createRandomValueProvider());
    }

    public static AddressBuilder createAddressBuilder(ValueProvider values) {
        return Address.builder()
                .zip(values.numericString(5))
                .city(values.fixedDecoratedString("city"))
                .street(values.fixedDecoratedString("street"))
                .number(values.intNumber(1, 500));
    }
}
