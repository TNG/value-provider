package com.tngtech.valueprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static com.tngtech.valueprovider.ValueProviderAsserter.setSeedProperties;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class DataProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(DataProviderTest.class);

    static {
        logger.debug("{}: static initialization", DataProviderTest.class.getSimpleName());
        ensureDefinedFactoryState();
    }

    /**
     * @see ValueProviderAsserter#setSeedProperties()
     */
    private static void ensureDefinedFactoryState() {
        setSeedProperties();
    }

    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();
    private static final ValueProvider classRandom1 = createRandomValueProvider();
    private static final ValueProvider classRandom2 = createRandomValueProvider();

    private static ValueProvider dataProviderRandom;
    private static ValueProvider beforeClassRandom;

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    private final ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeRandom;

    @BeforeClass
    public static void beforeClass() {
        beforeClassRandom = createRandomValueProvider();
    }

    @Before
    public void before() {
        beforeRandom = createRandomValueProvider();
    }

    @DataProvider
    public static Object[][] testValues() {
        logger.debug("create DataProvider");
        dataProviderRandom = createRandomValueProvider();
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @UseDataProvider("testValues")
    @Test
    public void test(String testValue) {
        assertThat(testValue).isNotEmpty();
    }

    @Test
    public void should_ensure_reproducible_ValueProvider_creation() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    public void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2, dataProviderRandom, beforeClassRandom)
                .addExpectedTestMethodRandomValues(instanceRandom, beforeRandom, createRandomValueProvider(), createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}
