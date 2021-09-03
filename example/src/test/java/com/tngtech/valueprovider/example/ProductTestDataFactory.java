package com.tngtech.valueprovider.example;

import com.tngtech.valueprovider.ValueProvider;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ProductTestDataFactory {

    private ProductTestDataFactory() {
    }

    public static Product createProduct() {
        return createProduct(createRandomValueProvider());
    }

    public static Product createProduct(ValueProvider values) {
        return Product.of(
                values.oneOf(ProductCategory.class),
                values.fixedDecoratedString("name"),
                values.fixedDecoratedString("description"));
    }
}
