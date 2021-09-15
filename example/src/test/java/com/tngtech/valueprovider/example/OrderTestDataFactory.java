package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.example.Order.OrderBuilder;

import static com.tngtech.valueprovider.ValueProvider.copyWithChangedPrefix;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static com.tngtech.valueprovider.example.AddressTestDataFactory.createAddress;
import static com.tngtech.valueprovider.example.CustomerTestDataFactory.createCustomer;
import static com.tngtech.valueprovider.example.OrderItemTestDataFactory.createOrderItem;

public final class OrderTestDataFactory {

    private OrderTestDataFactory() {
    }

    public static Order createOrder() {
        return createOrder(createRandomValueProvider());
    }

    public static Order createOrder(ValueProvider values) {
        return createOrderBuilder(values).build();
    }

    public static OrderBuilder createOrderBuilder() {
        return createOrderBuilder(createRandomValueProvider());
    }

    public static OrderBuilder createOrderBuilder(ValueProvider values) {
        OrderBuilder builder = Order.builder()
                .customer(createCustomer(values));
        setAddress(builder, values);
        addItems(builder, values);
        return builder;
    }

    private static void setAddress(OrderBuilder builder, ValueProvider values) {
        boolean useDifferentBillingAddress = values.booleanValue();
        if (useDifferentBillingAddress) {
            builder
                    .shippingAddress(createAddress(copyWithChangedPrefix(values, "S-")))
                    .billingAddress(createAddress(copyWithChangedPrefix(values, "B-")));

        } else {
            builder
                    .shippingAddress(createAddress(values));
        }
    }

    private static void addItems(OrderBuilder builder, ValueProvider values) {
        int numOrderItems = values.intNumber(1, 5);
        for (int i = 0; i < numOrderItems; i++) {
            char prefix = (char) ('A' + i);
            ValueProvider prefixedProvider = copyWithChangedPrefix(values, "" + prefix + "-");
            builder.orderItem(createOrderItem(prefixedProvider));
        }
    }
}
