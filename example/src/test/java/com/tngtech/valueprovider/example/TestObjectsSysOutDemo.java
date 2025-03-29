package com.tngtech.valueprovider.example;

import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.example.AddressModel.ADDRESS;
import static com.tngtech.valueprovider.example.AddressTestDataFactory.createAddress;
import static com.tngtech.valueprovider.example.OrderModel.ORDER;
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrder;
import static com.tngtech.valueprovider.example.ProductModel.PRODUCT;
import static com.tngtech.valueprovider.example.ProductTestDataFactory.createProduct;
import static java.lang.System.lineSeparator;

@ExtendWith(InstancioExtension.class)
class TestObjectsSysOutDemo {
    @Test
    void demo_of_value_provider_products() {
        System.out.println(createProduct());
        System.out.println(createProduct());
        System.out.println(createProduct());
    }

    @Test
    void demo_of_Instancio_products() {
        System.out.println(Instancio.of(PRODUCT).create());
        System.out.println(Instancio.of(PRODUCT).create());
        System.out.println(Instancio.of(PRODUCT).create());
    }

    @Test
    void demo_of_value_provider_addresses() {
        System.out.println(createAddress());
        System.out.println(createAddress());
        System.out.println(createAddress());
    }

    @Test
    void demo_of_Instancio_addresses() {
        System.out.println(Instancio.of(ADDRESS).create());
        System.out.println(Instancio.of(ADDRESS).create());
        System.out.println(Instancio.of(ADDRESS).create());
    }

    @Test
    void demo_of_Instancio_orders() {
        System.out.println(format(Instancio.of(ORDER).create()));
        System.out.println(format(Instancio.of(ORDER).create()));
        System.out.println(format(Instancio.of(ORDER).create()));
    }

    @Test
    void demo_of_value_provider_orders() {
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
