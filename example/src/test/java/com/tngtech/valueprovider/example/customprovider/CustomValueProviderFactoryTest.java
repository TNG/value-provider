package com.tngtech.valueprovider.example.customprovider;

import java.util.Random;

import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("JoinAssertThatStatements")
@ExtendWith(ValueProviderExtension.class)
class CustomValueProviderFactoryTest {
    @Test
    void should_create_instances_of_custom_class() {
        Object customRandom = CustomValueProviderFactory.createRandomValueProvider();
        Object customReproducible = CustomValueProviderFactory.createReproducibleValueProvider(42L);

        assertThat(customRandom).isInstanceOf(CustomValueProvider.class);
        assertThat(customReproducible).isInstanceOf(CustomValueProvider.class);
    }

    @Test
    void createRandomValueProvider_should_create_providers_that_return_different_sequence_of_data() {
        CustomValueProvider customRandom1 = CustomValueProviderFactory.createRandomValueProvider();
        CustomValueProvider customRandom2 = CustomValueProviderFactory.createRandomValueProvider();

        assertThat(customRandom1.myCustomValue()).isNotEqualTo(customRandom2.myCustomValue());
        assertThat(customRandom1.myCustomValue()).isNotEqualTo(customRandom2.myCustomValue());
    }

    @Test
    void createReproducibleValueProvider_should_create_providers_that_return_same_sequence_of_data() {
        long seed = new Random().nextLong();
        CustomValueProvider customReproducible1 = CustomValueProviderFactory.createReproducibleValueProvider(seed);
        CustomValueProvider customReproducible2 = CustomValueProviderFactory.createReproducibleValueProvider(seed);

        assertThat(customReproducible1.myCustomValue()).isEqualTo(customReproducible2.myCustomValue());
        assertThat(customReproducible1.myCustomValue()).isEqualTo(customReproducible2.myCustomValue());
    }
}