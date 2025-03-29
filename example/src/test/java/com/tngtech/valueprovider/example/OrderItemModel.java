package com.tngtech.valueprovider.example;

import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;

import static com.tngtech.valueprovider.example.ProductModel.PRODUCT;
import static lombok.AccessLevel.PRIVATE;
import static org.instancio.Select.field;

@NoArgsConstructor(access = PRIVATE)
public class OrderItemModel {
    public static final Model<OrderItem> ORDER_ITEM = Instancio.of(OrderItem.class)
            .generate(field(OrderItem::getQuantity), gen -> gen.ints().range(1, 100))
            .setModel(field(OrderItem::getProduct), PRODUCT)
            .toModel();
}
