package com.tngtech.valueprovider.dataprovider1;

import com.tngtech.valueprovider.ValueProvider;
import org.slf4j.Logger;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public final class DummyDataProviderTests {
    private static final String RANDOM_FAILURE_PROPERTY = "randomFailureForDataProviderTests";

    /**
     * Dummy dataprovider to allow manual verification of proper switching between test cycles for tests with
     * {@link com.tngtech.java.junit.dataprovider.DataProvider}s.
     * <p>
     * Specify system property -DrandomFailureForDataProviderTests=true for random test failures.
     */
    public static Object[][] createNonEmptyTestValues(Logger logger) {
        logger.debug("create DataProvider");
        ValueProvider values = createRandomValueProvider();
        boolean failuerPropertySet = "true".equals(System.getProperty(RANDOM_FAILURE_PROPERTY));
        String randomlyFailing = failuerPropertySet && values.booleanValue() ? "" : "not empty";
        return $$(
                $(values.fixedDecoratedString("1")),
                $(randomlyFailing),
                $(values.fixedDecoratedString("2"))
        );
    }
}
