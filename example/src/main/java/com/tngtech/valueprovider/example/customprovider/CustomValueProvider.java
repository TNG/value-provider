package com.tngtech.valueprovider.example.customprovider;

import com.tngtech.valueprovider.AbstractValueProvider;
import com.tngtech.valueprovider.ValueProviderInitialization;

/**
 * @see CustomValueProviderFactory
 */
@SuppressWarnings("WeakerAccess")
public class CustomValueProvider extends AbstractValueProvider<CustomValueProvider> {
    CustomValueProvider(ValueProviderInitialization initialization) {
        super(initialization);
    }

    /**
     * Just for demonstration purposes, a project-specific method to provide syntactically valid AGS values
     * (as used by German municipial administration).
     */
    public String myCustomValue() {
        return String.format("%02d%s", intNumber(1, 16), numericString(6));
    }
}
