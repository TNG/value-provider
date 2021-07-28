package com.tngtech.valueprovider.example;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class Product {
    @NonNull
    private final ProductCategory category;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
}
