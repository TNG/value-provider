package com.tngtech.valueprovider;

import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.tngtech.valueprovider.InitializationCreator.DefaultInitializationCreator;
import com.tngtech.valueprovider.InitializationCreator.TestClassInitializationCreator;
import com.tngtech.valueprovider.InitializationCreator.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;
import static com.tngtech.valueprovider.InitializationCreator.TestMethodInitializationCreator;
import static com.tngtech.valueprovider.InitializationCreator.Type.DEFAULT;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_CLASS;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_METHOD;
import static java.lang.System.identityHashCode;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public final class ValueProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(ValueProviderFactory.class);

    private static final ThreadLocal<ValueProviderFactory> INSTANCE = ThreadLocal.withInitial(ValueProviderFactory::new);

    private static final Map<Type, Set<Type>> LEGAL_TRANSITIONS = ImmutableMap.of(
            DEFAULT, ImmutableSet.of(TEST_CLASS, TEST_METHOD),
            // TEST_CLASS -> TEST_CLASS happens e.g. in discovery phase of DataProviderRunner, which loads EVERY test class
            TEST_CLASS, ImmutableSet.of(TEST_CLASS, TEST_METHOD),
            TEST_METHOD, ImmutableSet.of()
    );

    private final DefaultInitializationCreator defaultVpiCreator = new DefaultInitializationCreator();
    private TestClassInitializationCreator testClassVpiCreator;
    private final TestMethodInitializationCreator testMethodVpiCreator = new TestMethodInitializationCreator();
    @VisibleForTesting
    InitializationCreator activeVpiCreator = defaultVpiCreator;
    @VisibleForTesting
    InitializationCreator previousVpiCreator = defaultVpiCreator;

    private ValueProviderFactory() {
        logger.debug("Instantiation {}", identityHashCode(this));
    }

    private TestClassInitializationCreator doStartTestClassCycle() {
        checkMayStart(TEST_CLASS);
        TestClassInitializationCreator testClassVpiCreator = new TestClassInitializationCreator();
        doActivateOrSwitchTestClassCreator(testClassVpiCreator);
        testClassVpiCreator.startTestCycle(empty());
        return testClassVpiCreator;
    }

    private void doResumeTestClassCycle(TestClassInitializationCreator testClassVpiCreator) {
        checkMayStart(TEST_CLASS);
        doActivateOrSwitchTestClassCreator(testClassVpiCreator);
    }

    private void doFinishTestClassCycle() {
        checkMayFinish(TEST_CLASS);
        testClassVpiCreator.finishTestCycle();
        doActivateCreator(defaultVpiCreator);
        resetTestClassVpiCreator();
    }

    private void doStartTestMethodCycle() {
        checkMayStart(TEST_METHOD);
        testMethodVpiCreator.startTestCycle(ofNullable(testClassVpiCreator));
        doActivateCreator(testMethodVpiCreator);
    }

    private void doFinishTestMethodCycle() {
        checkMayFinish(TEST_METHOD);
        doActivateCreator(previousVpiCreator);
        testMethodVpiCreator.finishTestCycle();
    }

    private void checkMayStart(Type type) {
        Type activeType = activeVpiCreator.getType();
        Set<Type> legalTransitions = LEGAL_TRANSITIONS.get(activeType);
        checkState(legalTransitions.contains(type),
                "Illegal transition, cannot switch from %s to %s (legal transitions %s)",
                activeType, type, legalTransitions);
    }

    private void checkMayFinish(Type type) {
        Type activeType = activeVpiCreator.getType();
        checkState(activeType == type,
                "Illegal transition, cannot switch from %s back to %s", activeType, type);
    }

    private void doResetToDefaultVpiCreator() {
        logger.debug("{} doResetToDefaultValueCreator (active {}, previous{})",
                identityHashCode(this),
                activeVpiCreator.getId(),
                previousVpiCreator.getId());
        previousVpiCreator = defaultVpiCreator;
        activeVpiCreator = defaultVpiCreator;
        defaultVpiCreator.finishTestCycle();
        if (testClassVpiCreator != null) {
            testClassVpiCreator.finishTestCycle();
            resetTestClassVpiCreator();
        }
        testMethodVpiCreator.finishTestCycle();
    }

    private void doActivateOrSwitchTestClassCreator(TestClassInitializationCreator newTestClassVpiCreator) {
        TestClassInitializationCreator previousTestClassVpiCreator = testClassVpiCreator;
        testClassVpiCreator = newTestClassVpiCreator;
        if (previousTestClassVpiCreator == null) {
            doActivateCreator(testClassVpiCreator);
        } else {
            doSwitchTestClassCreator(previousTestClassVpiCreator, testClassVpiCreator);
        }
    }

    private void doActivateCreator(InitializationCreator creator) {
        logger.debug("{} activateCreator from {} to {}", identityHashCode(this),
                activeVpiCreator.getId(), creator.getId());
        previousVpiCreator = activeVpiCreator;
        activeVpiCreator = creator;
    }

    private void doSwitchTestClassCreator(TestClassInitializationCreator from, TestClassInitializationCreator to) {
        logger.debug("{} switchTestClassCreator from {} to {}", identityHashCode(this),
                identityHashCode(from), identityHashCode(to));
        // previousVpiCreator NOT changed as it should only reflect cycle transitions
        activeVpiCreator = to;
    }

    private void resetTestClassVpiCreator() {
        testClassVpiCreator = null;
    }

    @VisibleForTesting
    static void initializeTestClassSeed() {
        instance().testClassVpiCreator.initialize();
    }

    @VisibleForTesting
    static void resetToDefaultVpiCreator() {
        instance().doResetToDefaultVpiCreator();
    }

    static TestCycle startTestClassCycle() {
        return instance().doStartTestClassCycle();
    }

    static void finishTestClassCycle() {
        instance().doFinishTestClassCycle();
    }

    static void resumeTestClassCycle(TestCycle testCycle) {
        instance().doResumeTestClassCycle((TestClassInitializationCreator) testCycle);
    }

    static TestCycleSnapshot takeTestClassCycleSnapshot(TestCycle testCycle) {
        return ((TestClassInitializationCreator) testCycle).takeSnapshot();
    }

    static void resumeTestClassCycle(TestCycleSnapshot testCycleSnapshot) {
        instance().doResumeTestClassCycle(new TestClassInitializationCreator((InitializationCreatorSnapshot) testCycleSnapshot));
    }

    static void startTestMethodCycle() {
        instance().doStartTestMethodCycle();
    }

    static void finishTestMethodCycle() {
        instance().doFinishTestMethodCycle();
    }

    /**
     * Creates a random {@link ValueProviderInitialization}. Used to create the initialization for custom {@link ValueProvider} implementations, that extend the {@link ValueProvider}.
     *
     * @return a random initialization that is used in the constructor of an (extension of) {@link ValueProvider}.
     */
    public static ValueProviderInitialization createRandomValueProviderInitialization() {
        return instance().doCreateRandomValueProviderInitialization();
    }

    private ValueProviderInitialization doCreateRandomValueProviderInitialization() {
        return activeVpiCreator.createRandomValueProviderInitialization();
    }

    /**
     * Factory method, that creates a random {@link ValueProvider}.
     *
     * @return a {@link ValueProvider} with random seed, suffix and no prefix.
     * @see #createReproducibleValueProvider(long)
     */
    public static ValueProvider createRandomValueProvider() {
        return new ValueProvider(createRandomValueProviderInitialization());
    }

    /**
     * Factory method, that creates a {@link ValueProvider} using {@code seed}.
     * <p>
     * If the method is called multiple times with the same {@code seed}, the returned {@link ValueProvider}
     * will provide the same (sequence of) result(s) for the same (sequence of) method invocation(s).
     * </p>
     *
     * @return a {@link ValueProvider} using {@code seed}, the respective suffix, and no prefix.
     * @see #createRandomValueProvider()
     */
    public static ValueProvider createReproducibleValueProvider(long seed) {
        return new ValueProvider(createReproducibleValueProviderInitialization(seed));
    }

    /**
     * Creates a reproducible {@link ValueProviderInitialization} using {@code seed}. Used to create the initialization for custom {@link ValueProvider} implementations, that extend the {@link ValueProvider}.
     *
     * @return an initialization that is used in the constructor of an (extension of) {@link ValueProvider}.
     */
    public static ValueProviderInitialization createReproducibleValueProviderInitialization(long seed) {
        return ValueProviderInitialization.createReproducibleInitialization(seed);
    }

    /**
     * @return seed value for test class cycle or 0, if test class cycle is currently not active
     */
    static long getTestClassSeed() {
        TestClassInitializationCreator testClassVpiCreator = instance().testClassVpiCreator;
        return getSeedOrZero(testClassVpiCreator);
    }

    private static long getSeedOrZero(InitializationCreator creator) {
        return ofNullable(creator)
                .map(InitializationCreator::getSeed)
                .orElse(0L);
    }

    /**
     * @return seed value for (last) test method cycle
     */
    static long getTestMethodSeed() {
        return instance().testMethodVpiCreator.getSeed();
    }

    static String getFormattedReferenceDateTime() {
        return instance().testMethodVpiCreator.getFormattedReferenceDateTime();
    }

    @VisibleForTesting
    static ValueProviderFactory instance() {
        return INSTANCE.get();
    }
}
