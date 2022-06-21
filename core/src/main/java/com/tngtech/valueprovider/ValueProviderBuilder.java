package com.tngtech.valueprovider;

import java.time.LocalDateTime;

public interface ValueProviderBuilder<
        VP extends AbstractValueProvider<VP>,
        BUILDER extends ValueProviderBuilder<VP, BUILDER>> {

    BUILDER withConstantPrefix(String prefix);

    BUILDER withConstantSuffix(String suffix);

    BUILDER withReferenceLocalDateTime(LocalDateTime referenceLocalDateTime);

    VP build();
}
