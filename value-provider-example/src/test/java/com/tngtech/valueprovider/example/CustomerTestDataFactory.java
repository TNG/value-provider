package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.example.Customer.CustomerBuilder;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public final class CustomerTestDataFactory {

    private CustomerTestDataFactory() {
    }

    public static Customer createCustomer() {
        return createCustomer(createRandomValueProvider());
    }

    public static Customer createCustomer(ValueProvider values) {
        return createCustomerBuilder(values).build();
    }

    public static CustomerBuilder createCustomerBuilder() {
        return createCustomerBuilder(createRandomValueProvider());
    }

    public static CustomerBuilder createCustomerBuilder(ValueProvider values) {
        return Customer.builder()
                .firstName(values.fixedDecoratedString("firstName"))
                .lastName(values.fixedDecoratedString("lastName"))
                .birthDate(values.fixedLocalDate()
                        .minusYears(values.intNumber(18, 100))
                        .minusDays(values.intNumber(0, 365)));
    }
}
