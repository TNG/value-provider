package com.tngtech.valueprovider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.InitializationCreator.Type.DEFAULT;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_CLASS;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_METHOD;
import static com.tngtech.valueprovider.InitializationCreatorSnapshot.truncateToSupportedResolution;
import static com.tngtech.valueprovider.ValueProvider.Builder.createSuffix;
import static java.lang.System.identityHashCode;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

abstract class InitializationCreator {
    static final String VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY = "value.provider.factory.test.class.seed";
    static final String VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY = "value.provider.factory.test.method.seed";
    static final String VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY = "value.provider.factory.reference.date.time";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISO_LOCAL_DATE_TIME;

    enum Type {
        DEFAULT(),
        TEST_CLASS(VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY),
        TEST_METHOD(VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY);

        final Optional<String> seedPropertyName;

        Type() {
            this(null);
        }

        Type(String seedPropertyName) {
            this.seedPropertyName = Optional.ofNullable(seedPropertyName);
        }
    }

    // 3-letter suffix allows ~140.000 combinations
    private static final int MAX_SUFFIX_RETRIES = 50_000;

    private static final Logger logger = LoggerFactory.getLogger(InitializationCreator.class);
    private final Type type;
    private final String creatorId;

    private RandomValuesSequence randomSequence;
    private LocalDateTime referenceDateTime;
    final Set<String> suffixes = new HashSet<>();

    InitializationCreator(Type type) {
        this.type = type;
        this.creatorId = creatorId(type.name());
        initialize();
    }

    private String creatorId(String name) {
        return name + "(" + identityHashCode(this) + ")";
    }

    String getId() {
        return creatorId;
    }

    Type getType() {
        return type;
    }

    void initialize() {
        createRandom(type.seedPropertyName);
        calculateReferenceDateTime();
    }

    private void createRandom(Optional<String> propertyName) {
        createRandom(calculateSeed(propertyName));
    }

    private long calculateSeed(Optional<String> propertyName) {
        return getSeedProperty(propertyName).orElse(new Random().nextLong());
    }

    private Optional<Long> getSeedProperty(Optional<String> propertyName) {
        return propertyName
                .map(System::getProperty)
                .map(Long::valueOf);
    }

    private void createRandom(long seed) {
        logger.debug("{} createRandom(), seed {}", creatorId, seed);
        randomSequence = new RandomValuesSequence(seed);
    }

    private void calculateReferenceDateTime() {
        referenceDateTime = truncateToSupportedResolution(getReferenceDateTimeProperty().orElse(LocalDateTime.now()));
        logger.debug("{} calculateReferenceDateTime({})", creatorId, getFormattedReferenceDateTime());
    }

    private Optional<LocalDateTime> getReferenceDateTimeProperty() {
        return Optional.ofNullable(System.getProperty(VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY))
                .map(this::parseReferenceDateTime);
    }

    private LocalDateTime parseReferenceDateTime(String from) {
        return LocalDateTime.parse(from, DATE_TIME_FORMATTER);
    }

    void copyRelevantData(InitializationCreator from) {
        copyValueProviderSuffixes(from.suffixes);
        copyReferenceDateTime(from.referenceDateTime);
    }

    void copyRelevantData(InitializationCreatorSnapshot from) {
        createRandom(from.getSeed());
        copyReferenceDateTime(from.getReferenceDateTime());
    }

    private void copyValueProviderSuffixes(Set<String> toCopy) {
        logger.debug("{} copyValueProviderSuffixes({})", creatorId, toCopy);
        suffixes.addAll(toCopy);
    }

    private void copyReferenceDateTime(LocalDateTime referenceDateTime) {
        this.referenceDateTime = referenceDateTime;
        logger.debug("{} copyReferenceDateTime({})", creatorId, getFormattedReferenceDateTime());
    }

    void resetValueProviderSuffixes() {
        logger.debug("{} resetValueProviderSuffixes()", creatorId);
        suffixes.clear();
    }

    ValueProviderInitialization createRandomValueProviderInitialization() {
        return createRandomValueProviderInitializationUniqueSuffix();
    }

    ValueProviderInitialization createRandomValueProviderInitializationArbitrarySuffix() {
        RandomValues random = createNextRandom();
        String suffix = createSuffix(random);
        return createRandomValueProviderInitialization(random, "arbitrary", suffix);
    }

    private ValueProviderInitialization createRandomValueProviderInitializationUniqueSuffix() {
        RandomValues random;
        String suffix;
        int retries = 0;
        do {
            random = createNextRandom();
            suffix = createSuffix(random);
            retries++;
            if (retries > MAX_SUFFIX_RETRIES) {
                String error = String.format("Cannot find unused suffix, giving up after %d retries, "
                                + "think about reducing the number of %s instances.",
                        MAX_SUFFIX_RETRIES, ValueProvider.class.getSimpleName());
                throw new RuntimeException(error);
            }
        } while (!suffixes.add(suffix));
        return createRandomValueProviderInitialization(random, "unique", suffix);
    }

    private RandomValues createNextRandom() {
        RandomValues random = randomSequence.nextRandomValues();
        logger.debug("{} createNextRandom(), {}({})", creatorId, randomSequence.getSequenceCounter(), random.getSeed());
        return random;
    }

    private ValueProviderInitialization createRandomValueProviderInitialization(RandomValues random, String suffixHandling, String suffix) {
        logger.debug("{} createRandomValueProviderInitialization(), creator-seed {}, provider-seed {}, {} provider-suffix {}",
                creatorId,
                this.randomSequence.getSeed(), random.getSeed(),
                suffixHandling, suffix);
        return new ValueProviderInitialization(random, suffix, referenceDateTime);
    }

    long getSeed() {
        return randomSequence.getSeed();
    }

    long getSequenceCounter() {
        return randomSequence.getSequenceCounter();
    }

    LocalDateTime getReferenceDateTime() {
        return referenceDateTime;
    }

    String getFormattedReferenceDateTime() {
        return referenceDateTime.format(DATE_TIME_FORMATTER);
    }

    void startTestCycle(Optional<? extends InitializationCreator> surroundingState) {
        initialize();
        resetValueProviderSuffixes();
    }

    void finishTestCycle() {
        // do NOT reset suffixes here by default
        // * irrelevant for DefaultInitializationCreator
        // * counter productive for TestClassInitializationCreator, as it would sacrifice
        //   even the limited repeated test execution support for JUnit4
        // * only required for TestMethodInitializationCreator
    }

    static class DefaultInitializationCreator extends InitializationCreator {
        DefaultInitializationCreator() {
            super(DEFAULT);
        }

        @Override
        ValueProviderInitialization createRandomValueProviderInitialization() {
            return createRandomValueProviderInitializationArbitrarySuffix();
        }
    }

    static class TestClassInitializationCreator extends InitializationCreator implements TestCycle {
        TestClassInitializationCreator(InitializationCreatorSnapshot from) {
            this();
            restore(from);
        }

        private void restore(InitializationCreatorSnapshot from) {
            copyRelevantData(from);
            for (long i = 0; i < from.getSequenceCounter(); i++) {
                createRandomValueProviderInitialization();
            }
        }

        TestClassInitializationCreator() {
            super(TEST_CLASS);
        }

        InitializationCreatorSnapshot takeSnapshot() {
            return new InitializationCreatorSnapshot(getSeed(), getReferenceDateTime(), getSequenceCounter());
        }
    }

    static class TestMethodInitializationCreator extends InitializationCreator {
        TestMethodInitializationCreator() {
            super(TEST_METHOD);
        }

        @Override
        void startTestCycle(Optional<? extends InitializationCreator> surroundingCreator) {
            super.startTestCycle(surroundingCreator);
            surroundingCreator.ifPresent(this::copyRelevantData);
        }

        @Override
        void finishTestCycle() {
            super.finishTestCycle();
            resetValueProviderSuffixes();
        }
    }
}
