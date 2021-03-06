package com.tngtech.valueprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderExtension.TestMethodCycleState.BEFORE_FIRST_CYCLE;
import static com.tngtech.valueprovider.ValueProviderExtension.TestMethodCycleState.CYCLE_COMLETED;
import static com.tngtech.valueprovider.ValueProviderExtension.TestMethodCycleState.CYCLE_STARTED;
import static java.lang.System.identityHashCode;

public class ValueProviderExtension implements
        BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback,
        TestExecutionExceptionHandler,
        InvocationInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ValueProviderExtension.class);

    /**
     * Required to deal with @{@link Disabled}
     * test-methods, as the test-class is instantiated for them
     * (i.e. {@link #interceptTestClassConstructor(Invocation, ReflectiveInvocationContext, ExtensionContext)} invoked),
     * but neither of {@link #beforeEach(ExtensionContext)} nor esp. {@link #afterEach(ExtensionContext)} is invoked, but
     * either interceptTestClassConstructor() for the next test-method,
     * or afterAll() when the disabled test-method was the last to be executed
     */
    enum TestMethodCycleState {
        BEFORE_FIRST_CYCLE,
        CYCLE_STARTED,
        CYCLE_COMLETED
    }

    private TestMethodCycleState testMethodCycleState = BEFORE_FIRST_CYCLE;

    @Override
    public void beforeAll(ExtensionContext context) {
        logger.debug("{} beforeAll {}",
                identityHashCode(this), getTestClassName(context));
        startTestClassCycle();
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        logger.debug("{} interceptTestClassConstructor {}",
                identityHashCode(this), buildQualifiedTestMethodName(extensionContext));
        ensureStaticInitializationOfTestClass(extensionContext);
        startTestMethodCycle();
        return invocation.proceed();
    }

    private void ensureStaticInitializationOfTestClass(ExtensionContext extensionContext) throws ClassNotFoundException {
        Class<?> testClass = getTestClass(extensionContext);
        // the following statement may seem meaningless,
        // but as tests show, depending on the static code of a test,
        // the testClass is NOT necessarily initialized at this stage of test execution
        // e.g. a @BeforeAll method ensures testClass initialization, static code does NOT
        Class.forName(testClass.getName());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        logger.debug("{} beforeEach {}",
                identityHashCode(this), buildQualifiedTestMethodName(context));
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        logger.debug("{} handleTestExecutionException {}",
                identityHashCode(this), buildQualifiedTestMethodName(context));
        // Note: handleTestExecutionException() is invoked BEFORE afterEach, i.e. BEFORE seed is reset,
        // so that the correct seed values appear in the failure message
        throwable.addSuppressed(new ValueProviderException());
        throw throwable;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        logger.debug("{} afterEach {}",
                identityHashCode(this), buildQualifiedTestMethodName(context));
        finishTestMethodCycle();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        logger.debug("{} afterAll {}",
                identityHashCode(this), getTestClassName(context));
        finishTestClassCycle();
    }

    private void startTestClassCycle() {
        ValueProviderFactory.startTestClassCycle();
        resetTestMethodCycleState();
    }

    private void startTestMethodCycle() {
        finishTestMethodCycleIfNecessary();
        ValueProviderFactory.startTestMethodCycle();
        testMethodCycleState = CYCLE_STARTED;
    }

    private void finishTestClassCycle() {
        finishTestMethodCycleIfNecessary();
        ValueProviderFactory.finishTestClassCycle();
        resetTestMethodCycleState();
    }

    private void finishTestMethodCycleIfNecessary() {
        if (testMethodCycleState == CYCLE_STARTED) {
            // was started, but not finished due to @Disabled test-method
            finishTestMethodCycle();
        }
    }

    private void finishTestMethodCycle() {
        ValueProviderFactory.finishTestMethodCycle();
        testMethodCycleState = CYCLE_COMLETED;
    }

    private void resetTestMethodCycleState() {
        testMethodCycleState = BEFORE_FIRST_CYCLE;
    }

    static String buildQualifiedTestMethodName(ExtensionContext context) {
        return String.format("%s.%s", getTestClassName(context), getTestMethodName(context));
    }

    private static String getTestClassName(ExtensionContext context) {
        return getTestClass(context).getSimpleName();
    }

    private static Class<?> getTestClass(ExtensionContext context) {
        return context.getTestClass()
                .orElseThrow(() -> new IllegalArgumentException("cannot determine test class"));
    }

    private static String getTestMethodName(ExtensionContext context) {
        return context.getTestMethod()
                .map(Method::getName)
                .orElse("<unknown>");
    }
}
