package com.tngtech.valueprovider.example;

import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static org.instancio.Select.field;

@NoArgsConstructor(access = PRIVATE)
public final class CustomerModel {
    public static final Model<Customer> CUSTOMER = Instancio.of(Customer.class)
            .supply(field(Customer::getBirthDate), random ->
                    LocalDate.now()
                            .minusYears(random.intRange(18, 100))
                            .minusDays(random.intRange(0, 365)))
            .toModel();
}
