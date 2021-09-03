package com.tngtech.valueprovider.dataprovider1;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.valueprovider.ValueProviderClassRule;
import com.tngtech.valueprovider.ValueProviderRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.dataprovider1.DummyDataProviderTests.createNonEmptyTestValues;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@RunWith(DataProviderRunner.class)
public class DummyDataProviderTest11 {
    private static final Logger logger = LoggerFactory.getLogger(DummyDataProviderTest11.class);

    static {
        logger.debug("{}: static initialization", DummyDataProviderTest11.class.getSimpleName());
    }

    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    @DataProvider
    public static Object[][] testValues() {
        return createNonEmptyTestValues(logger);
    }

    @UseDataProvider("testValues")
    @Test
    public void test(String testValue) {
        assertThat(testValue).isNotEmpty();
    }
}
