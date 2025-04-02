package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.example.Order.OrderBuilder;

import java.util.List;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static com.tngtech.valueprovider.example.AddressTestDataFactory.createAddress;
import static com.tngtech.valueprovider.example.CustomerTestDataFactory.createCustomer;
import static java.lang.String.format;

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
                .customer(createCustomer(values))
                .orderItems(createItems(values));
        setAddresses(builder, values);
        return builder;
    }

    private static List<OrderItem> createItems(ValueProvider values) {
        return values.collection()
                .numElements(1, 5)
                .replacePrefixVia(i -> format("%c", (char) ('A' + i)))
                .listOf(OrderItemTestDataFactory::createOrderItem);
    }

    private static void setAddresses(OrderBuilder builder, ValueProvider values) {
        boolean useDifferentBillingAddress = values.booleanValue();
        if (useDifferentBillingAddress) {
            builder
                    .shippingAddress(createAddress(values.copyWithChangedPrefix("S-")))
                    .billingAddress(createAddress(values.copyWithChangedPrefix("B-")));

        } else {
            builder
                    .shippingAddress(createAddress(values));
        }
    }
}
