package com.tngtech.valueprovider.example;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Order {
    @Singular
    @NonNull
    private final ImmutableList<OrderItem> orderItems;
    @NonNull
    private final Customer customer;
    @NonNull
    private final Address shippingAddress;
    @NonNull
    private final Optional<Address> billingAddress;

    public Address getBillingAddress() {
        return billingAddress.orElse(shippingAddress);
    }

    public static class OrderBuilder {
        private Optional<Address> billingAddress = Optional.empty();

        public OrderBuilder billingAddress(Address billingAddress) {
            return billingAddress(Optional.of(billingAddress));
        }

        public OrderBuilder billingAddress(Optional<Address> billingAddress) {
            this.billingAddress = billingAddress;
            return this;
        }

        public ImmutableList.Builder<OrderItem> getOrderItems() {
            return orderItems;
        }

        public Customer getCustomer() {
            return customer;
        }

        public Address getShippingAddress() {
            return shippingAddress;
        }

        public Optional<Address> getBillingAddress() {
            return billingAddress;
        }
    }
}
