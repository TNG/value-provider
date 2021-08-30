package com.tngtech.valueprovider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.InitializationCreator.TestClassInitializationCreator;
import static com.tngtech.valueprovider.InitializationCreator.TestMethodInitializationCreator;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreatorSnapshot.truncateToSupportedResolution;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

class InitializationCreatorTest {

    @Test
    void TestClassInitializationCreator_should_use_data_set_via_properties_for_random_data() {
        long seed1 = 1L;
        LocalDateTime referenceDateTime1 = LocalDateTime.now();
        setTestClassProperties(seed1, referenceDateTime1);
        TestClassInitializationCreator testClassCreator = new TestClassInitializationCreator();
        ValueProviderInitialization initialization = testClassCreator.createRandomValueProviderInitialization();

        // second ValueProvider must get same suffix
        testClassCreator.startTestCycle(empty());
        ValueProviderInitialization shouldBeEqual = testClassCreator.createRandomValueProviderInitialization();

        long seed2 = 42L;
        LocalDateTime referenceDateTime2 = referenceDateTime1.plusSeconds(43L);
        setTestClassProperties(seed2, referenceDateTime2);
        testClassCreator.startTestCycle(empty());
        ValueProviderInitialization shouldBeDifferent = testClassCreator.createRandomValueProviderInitialization();

        assertThat(initialization).isEqualTo(shouldBeEqual);
        assertDifferent(initialization, shouldBeDifferent);
    }

    @Test
    void TestMethodInitializationCreator_should_use_data_set_via_properties_for_random_data() {
        long seed1 = 2L;
        LocalDateTime referenceDateTime1 = LocalDateTime.now();
        setTestMethodProperties(seed1, referenceDateTime1);
        InitializationCreator.TestMethodInitializationCreator testMethodCreator = new TestMethodInitializationCreator();
        ValueProviderInitialization initialization = testMethodCreator.createRandomValueProviderInitialization();

        // second ValueProviderInitialization must get same suffix
        testMethodCreator.startTestCycle(empty());
        ValueProviderInitialization shouldBeEqual = testMethodCreator.createRandomValueProviderInitialization();

        long seed2 = 43L;
        LocalDateTime referenceDateTime2 = referenceDateTime1.plusMinutes(44L);
        setTestMethodProperties(seed2, referenceDateTime2);
        testMethodCreator.startTestCycle(empty());
        ValueProviderInitialization shouldBeDifferent = testMethodCreator.createRandomValueProviderInitialization();

        assertThat(initialization).isEqualTo(shouldBeEqual);
        assertDifferent(initialization, shouldBeDifferent);
    }

    private static void setTestClassProperties(long seed, LocalDateTime referenceDateTime) {
        System.setProperty(VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY, "" + seed);
        setReferenceDateTimeProperty(referenceDateTime);
    }

    private static void setTestMethodProperties(long seed, LocalDateTime referenceDateTime) {
        System.setProperty(VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY, "" + seed);
        setReferenceDateTimeProperty(referenceDateTime);
    }

    private void assertDifferent(ValueProviderInitialization initialization, ValueProviderInitialization shouldBeDifferent) {
        assertThat(initialization.getRandom()).isNotEqualTo(shouldBeDifferent.getRandom());
        assertThat(initialization.getSuffix()).isNotEqualTo(shouldBeDifferent.getSuffix());
        assertThat(initialization.getReferenceLocalDateTime()).isNotEqualTo(shouldBeDifferent.getReferenceLocalDateTime());
    }

    @Test
    void TestClassInitializationCreator_should_ensure_unique_suffixes_for_created_ValueProviders() {
        shouldEnsureUniqueSuffixes(new TestClassInitializationCreator());
    }

    @Test
    void TestMethodInitializationCreator_should_ensure_unique_suffixes_for_created_ValueProviders() {
        shouldEnsureUniqueSuffixes(new InitializationCreator.TestMethodInitializationCreator());
    }

    private void shouldEnsureUniqueSuffixes(InitializationCreator creator) {
        long limit = (long) Math.pow(26, ValueProvider.SUFFIX_LENGTH);
        Set<String> suffixes = new HashSet<>();

        for (long i = 0; i < limit / 5; i++) {
            ValueProviderInitialization initialization = creator.createRandomValueProviderInitialization();
            String suffix = initialization.getSuffix();
            assertThat(suffixes).doesNotContain(suffix);
            suffixes.add(suffix);
        }
    }

    @Test
    void TestMethodInitializationCreator_should_take_over_provided_suffixes() {
        TestClassInitializationCreator testClassCreator = new TestClassInitializationCreator();
        testClassCreator.startTestCycle(empty());
        testClassCreator.createRandomValueProviderInitialization();
        testClassCreator.createRandomValueProviderInitialization();
        assertThat(testClassCreator.suffixes).isNotEmpty();

        InitializationCreator.TestMethodInitializationCreator testMethodCreator = new TestMethodInitializationCreator();
        assertThat(testMethodCreator.suffixes).isEmpty();

        testMethodCreator.startTestCycle(Optional.of(testClassCreator));

        Set<String> expectedSuffixes = testClassCreator.suffixes;
        assertThat(testMethodCreator.suffixes).isEqualTo(expectedSuffixes);
    }

    @Test
    void TestMethodInitializationCreator_should_take_over_provided_referenceDateTime() {
        LocalDateTime referenceDateTime1 = LocalDateTime.now();
        setReferenceDateTimeProperty(referenceDateTime1);
        TestClassInitializationCreator testClassCreator = new TestClassInitializationCreator();
        testClassCreator.startTestCycle(empty());
        ValueProviderInitialization fromTestClassCreator = testClassCreator.createRandomValueProviderInitialization();

        LocalDateTime referenceDateTime2 = referenceDateTime1.plusMinutes(44L);
        setReferenceDateTimeProperty(referenceDateTime2);
        InitializationCreator.TestMethodInitializationCreator testMethodCreator = new TestMethodInitializationCreator();
        testMethodCreator.startTestCycle(Optional.of(testClassCreator));
        ValueProviderInitialization fromTestMethodCreator = testMethodCreator.createRandomValueProviderInitialization();

        assertThat(fromTestMethodCreator.getReferenceLocalDateTime()).isEqualTo(fromTestClassCreator.getReferenceLocalDateTime());
    }

    @Test
    void finishTestCycle_should_NOT_clear_suffixes_for_ValueProviders_for_TestClassInitializationCreator() {
        InitializationCreator creator = new TestClassInitializationCreator();
        creator.startTestCycle(empty());
        creator.createRandomValueProviderInitialization();
        creator.createRandomValueProviderInitialization();
        assertThat(creator.suffixes).isNotEmpty();
        ImmutableSet<String> suffixes = ImmutableSet.copyOf(creator.suffixes);

        creator.finishTestCycle();
        assertThat(creator.suffixes).isEqualTo(suffixes);
    }

    @Test
    void finishTestCycle_should_clear_suffixes_for_ValueProviders_for_TestMethodInitializationCreator() {
        InitializationCreator creator = new TestMethodInitializationCreator();
        creator.startTestCycle(empty());
        creator.createRandomValueProviderInitialization();
        creator.createRandomValueProviderInitialization();
        assertThat(creator.suffixes).isNotEmpty();

        creator.finishTestCycle();
        assertThat(creator.suffixes).isEmpty();
    }

    @Test
    void createSnapshot_should_capture_all_information_to_recreate_a_TestClassInitializationCreator() {
        TestClassInitializationCreator creator = new TestClassInitializationCreator();
        creator.startTestCycle(empty());
        creator.createRandomValueProviderInitialization();
        creator.createRandomValueProviderInitialization();
        InitializationCreatorSnapshot snapshot = creator.takeSnapshot();
        ImmutableList<ValueProviderInitialization> originalInitializationsAfterSnapshot =
                ImmutableList.of(creator.createRandomValueProviderInitialization(), creator.createRandomValueProviderInitialization(), creator.createRandomValueProviderInitialization());

        TestClassInitializationCreator recreated = new TestClassInitializationCreator(snapshot);
        ImmutableList<ValueProviderInitialization> recreatedInitializations =
                ImmutableList.of(recreated.createRandomValueProviderInitialization(), recreated.createRandomValueProviderInitialization(), recreated.createRandomValueProviderInitialization());

        assertThat(recreated.getSeed()).isEqualTo(creator.getSeed());
        assertThat(recreated.getReferenceDateTime()).isEqualTo(creator.getReferenceDateTime());
        assertThat(recreatedInitializations).isEqualTo(originalInitializationsAfterSnapshot);
    }

    @Test
    void TestClassInitializationCreator_should_truncate_referenceDateTime_to_allow_taking_snapshot() {
        LocalDateTime referenceDateTime = LocalDateTime.now();
        setReferenceDateTimeProperty(referenceDateTime);

        TestClassInitializationCreator initializedViaProperty = new TestClassInitializationCreator();
        assertThat(initializedViaProperty.getReferenceDateTime()).isEqualTo(truncateToSupportedResolution(referenceDateTime));
        initializedViaProperty.takeSnapshot();

        clearReferenceDateTimeProperty();
        TestClassInitializationCreator initializedToDefault = new TestClassInitializationCreator();
        assertThat(initializedToDefault.getReferenceDateTime()).isEqualTo(truncateToSupportedResolution(initializedToDefault.getReferenceDateTime()));
        initializedToDefault.takeSnapshot();
    }

    private static void clearReferenceDateTimeProperty() {
        System.clearProperty(VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY);
    }

    private static void setReferenceDateTimeProperty(LocalDateTime reference) {
        System.setProperty(VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY, reference.format(ISO_LOCAL_DATE_TIME));
    }
}
