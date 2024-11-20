package com.tngtech.valueprovider.dataprovider;

import java.util.ArrayList;
import java.util.List;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.UseDataProvider;
import com.tngtech.junit.dataprovider.UseDataProviderExtension;
import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static com.tngtech.valueprovider.JUnit5Tests.asListWithoutNulls;
import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodName.class)
@ExtendWith({ValueProviderExtension.class, UseDataProviderExtension.class})
class LifecyclePerClassDataProviderDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(LifecyclePerClassDataProviderDemoTest.class);
    private static final ValueProvider classRandom1;
    private static final ValueProvider classRandom2;

    static {
        logger.debug("{}: static initialization", LifecyclePerClassDataProviderDemoTest.class.getSimpleName());
        ensureDefinedFactoryState();
        classRandom1 = createRandomValueProvider();
        classRandom2 = createRandomValueProvider();
    }

    private final ValueProvider instanceRandom = createRandomValueProvider();
    private ValueProvider beforeAllRandom;
    private ValueProvider dataProviderRandom;
    private ValueProvider beforeEachRandom;
    private ValueProvider methodRandom;

    private final List<ValueProvider> randomsOfPreviousTestMethods = new ArrayList<>();

    @BeforeAll
    void beforeAll() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createRandomValueProvider();
    }

    @AfterEach
    void resetTestMethodRandoms() {
        dataProviderRandom = null;
        beforeEachRandom = null;
        methodRandom = null;
    }

    @DataProvider
    Object[][] testValues1() {
        logger.debug("create DataProvider 1");
        dataProviderRandom = createRandomValueProvider();
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues1")
    void a_should_ensure_reproducible_ValueProvider_creation_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(dataProviderRandom, beforeEachRandom, methodRandom);
    }

    @TestTemplate
    @UseDataProvider("testValues1")
    void b_should_ensure_reproducible_ValueProvider_creation_for_same_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        // @DataProvider is invoked ONCE BEFORE FIRST test method using it
        verifyReproducibleValueProviderCreation(beforeEachRandom, methodRandom);
    }

    @DataProvider
    Object[][] testValues2() {
        logger.debug("create DataProvider 2");
        dataProviderRandom = createRandomValueProvider();
        return $$(
                $(dataProviderRandom.fixedDecoratedString("1")),
                $(dataProviderRandom.fixedDecoratedString("2"))
        );
    }

    @TestTemplate
    @UseDataProvider("testValues2")
    void c_should_ensure_proper_separation_of_test_class_and_test_method_cycles_for_DataProvider(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(dataProviderRandom, beforeEachRandom, methodRandom);
    }

    @Test
    void d_should_ensure_reproducible_ValueProvider_creation() {
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(beforeEachRandom, methodRandom);
    }

    @Test
    void e_should_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(beforeEachRandom, methodRandom);
    }

    private void verifyReproducibleValueProviderCreation(ValueProvider... additionalMethodRandoms) {
        List<ValueProvider> additionalMethodRandomList = asListWithoutNulls(additionalMethodRandoms);
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2)
                .addExpectedTestMethodRandomValues(instanceRandom, beforeAllRandom)
                .addExpectedTestMethodRandomValues(randomsOfPreviousTestMethods)
                .addExpectedTestMethodRandomValues(additionalMethodRandomList)
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
        randomsOfPreviousTestMethods.addAll(additionalMethodRandomList);
    }
}
