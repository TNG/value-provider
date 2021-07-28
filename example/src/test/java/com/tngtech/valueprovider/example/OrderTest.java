package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static com.tngtech.valueprovider.example.OrderItemTestDataFactory.createOrderItem;
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrderBuilder;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ValueProviderExtension.class)
class OrderTest {
    @Test
    void getBillingAddress_should_return_shippingAddress_if_billingAddress_is_not_explicitly_set() {
        Order noExplicitBillingAddress = createOrderBuilder()
                .billingAddress(empty())
                .build();

        assertThat(noExplicitBillingAddress.getBillingAddress()).isEqualTo(noExplicitBillingAddress.getShippingAddress());
    }

    @Test
    void toBuilder_should_create_equal_bean_in_spite_of_logic_for_billingAddress() {
        Order orderWithoutBillingAddress = createOrderBuilder()
                .billingAddress(empty())
                .build();
        Order copy = orderWithoutBillingAddress.toBuilder().build();

        assertThat(copy).isEqualTo(orderWithoutBillingAddress);
    }

    @Test
    void adding_equal_OrderItem_twice_should_result_in_two_items_in_order() {
        ValueProvider values = createRandomValueProvider();
        OrderItem item = createOrderItem(values);
        OrderItem copy = OrderItem.of(item.getProduct(), item.getQuantity());
        Order order = createOrderBuilder(values)
                .clearOrderItems()
                .orderItem(item)
                .orderItem(copy)
                .build();

        assertThat(order.getOrderItems()).hasSize(2);
    }
}
