package com.tngtech.valueprovider.example;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.example.AddressTestDataFactory.createAddress;
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrder;
import static com.tngtech.valueprovider.example.ProductTestDataFactory.createProduct;
import static java.lang.System.lineSeparator;

class TestObjectsSysOutDemo {
    @Test
    void demo_of_random_products() {
        System.out.println(createProduct());
        System.out.println(createProduct());
        System.out.println(createProduct());
    }

    @Test
    void demo_of_random_addresses() {
        System.out.println(createAddress());
        System.out.println(createAddress());
        System.out.println(createAddress());
    }

    @Test
    void demo_of_random_orders() {
        System.out.println(format(createOrder()));
        System.out.println(format(createOrder()));
        System.out.println(format(createOrder()));
    }

    private String format(Order order) {
        return order.toString().replaceAll(
                "(orderItems=|customer=|billingAddress=|shippingAddress=|OrderItem\\()",
                lineSeparator() + "$1");
    }
}
