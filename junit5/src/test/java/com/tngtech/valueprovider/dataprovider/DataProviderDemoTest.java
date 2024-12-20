package com.tngtech.valueprovider.dataprovider;

import java.util.ArrayList;
import java.util.List;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.UseDataProvider;
import com.tngtech.junit.dataprovider.UseDataProviderExtension;
import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

// sequence of extensions does NOT seem to matter
@ExtendWith({ValueProviderExtension.class, UseDataProviderExtension.class})
public class DataProviderDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(DataProviderDemoTest.class);
    private static final ValueProvider classRandom1;
    private static final ValueProvider classRandom2;

    static {
        logger.debug("{}: static initialization", DataProviderDemoTest.class.getSimpleName());
        ensureDefinedFactoryState();
        classRandom1 = createRandomValueProvider();
        classRandom2 = createRandomValueProvider();
    }

    // as execution sequence of tests may vary
    private static final List<ValueProvider> dataProviderRandoms = new ArrayList<>();
    private static ValueProvider beforeAllRandom;

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeEachRandom;

    @BeforeAll
    static void beforeAll() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createRandomValueProvider();
    }

    @DataProvider
    public static Object[][] testValues1() {
        logger.debug("create DataProvider 1");
        ValueProvider dataProviderRandom = createRandomValueProvider();
        dataProviderRandoms.add(dataProviderRandom);
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues1")
    void should_ensure_reproducible_ValueProvider_creation_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    @DataProvider
    public static Object[][] testValues2() {
        logger.debug("create DataProvider 2");
        ValueProvider dataProviderRandom = createRandomValueProvider();
        dataProviderRandoms.add(dataProviderRandom);
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues2")
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2, beforeAllRandom)
                .addExpectedTestClassRandomValues(dataProviderRandoms)
                .addExpectedTestMethodRandomValues(
                        instanceRandom, beforeEachRandom, createRandomValueProvider(), createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}
