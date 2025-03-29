package com.tngtech.valueprovider.example;

import lombok.NoArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Model;

import static lombok.AccessLevel.PRIVATE;
import static org.instancio.Select.field;

@NoArgsConstructor(access = PRIVATE)
public final class AddressModel {
    public static final Model<Address> ADDRESS = Instancio.of(Address.class)
            .generate(field(Address::getZip), gen -> gen.string().length(5).digits())
            .generate(field(Address::getNumber), gen -> gen.ints().range(1, 500))
            .toModel();
}
