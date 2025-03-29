package com.tngtech.valueprovider.example;

import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Random;

import java.util.Optional;

import static com.tngtech.valueprovider.example.AddressModel.ADDRESS;
import static com.tngtech.valueprovider.example.CustomerModel.CUSTOMER;
import static com.tngtech.valueprovider.example.OrderItemModel.ORDER_ITEM;
import static java.util.Optional.empty;
import static lombok.AccessLevel.PRIVATE;
import static org.instancio.Select.field;

@NoArgsConstructor(access = PRIVATE)
public final class OrderModel {
    public static final Model<Order> ORDER = Instancio.of(Order.class)
            .setModel(field(Order::getCustomer), CUSTOMER)
            .setModel(field(Order::getShippingAddress), ADDRESS)
            .supply(field(Order::getBillingAddress), OrderModel::createBillingAddress)
            .supply(field(Order::getOrderItems), OrderModel::createOrderItems)
            .toModel();

    private static Optional<Address> createBillingAddress(Random random) {
        return random.trueOrFalse()
                ? Optional.of(Instancio.create(ADDRESS))
                : empty();
    }

    private static ImmutableList<OrderItem> createOrderItems(Random random) {
        int numOrderItems = random.intRange(1, 5);
        return ImmutableList.copyOf(Instancio.ofList(ORDER_ITEM).size(numOrderItems).create());
    }
}
