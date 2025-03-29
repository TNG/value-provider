package com.tngtech.valueprovider.example;

import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ProductModel {
    // Note: presently, there are no explicit property settings needed,
    // but providing and using a Model all the same throughout the project ensures that any future changes will take effect
    public static final Model<Product> PRODUCT = Instancio.of(Product.class)
            .toModel();
}
