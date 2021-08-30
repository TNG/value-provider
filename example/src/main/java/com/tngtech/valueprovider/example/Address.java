package com.tngtech.valueprovider.example;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Address {
    private final String zip;
    private final String city;
    private final String street;
    private final int number;
}
