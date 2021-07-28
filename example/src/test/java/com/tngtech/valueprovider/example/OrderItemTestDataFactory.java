package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static com.tngtech.valueprovider.example.ProductTestDataFactory.createProduct;

public class OrderItemTestDataFactory {

    private OrderItemTestDataFactory() {
    }

    public static OrderItem createOrderItem() {
        return createOrderItem(createRandomValueProvider());
    }

    public static OrderItem createOrderItem(ValueProvider values) {
        return OrderItem.of(
                createProduct(values),
                values.intNumber(1, 100));
    }
}
