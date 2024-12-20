package com.tngtech.valueprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderExtension.TestMethodCycleState.*;
import static java.lang.System.identityHashCode;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

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
        startTestClassCycleIf(context, PER_METHOD);
    }

    @Override
    public <T> T interceptTestClassConstructor(
            Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        logger.debug("{} interceptTestClassConstructor {}",
                identityHashCode(this), getTestClassName(extensionContext));
        startTestClassCycleIf(extensionContext, PER_CLASS);
        ensureStaticInitializationOfTestClass(extensionContext);
        startTestMethodCycle(extensionContext);
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
        // If the test class hierarchy of the failed test method contains any class(es) with Lifecycle PER_CLASS,
        // all test methods of this hierarchy must be re-run to reproduce the failure.
        // The root test class of the hierarchy must therefore be shown in the failure reproduction info.
        Optional<Class<?>> testClassToReRunForReproduction = getRootClassInHierarchyWithLifecyclePerClass(context);
        // Note: handleTestExecutionException() is invoked BEFORE afterEach, i.e. BEFORE seed is reset,
        // so that the correct seed values appear in the failure message
        throwable.addSuppressed(new ValueProviderException(testClassToReRunForReproduction));
        throw throwable;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        logger.debug("{} afterEach {}",
                identityHashCode(this), buildQualifiedTestMethodName(context));
        if (testClassHierarchyHasOnlyLifecyclePerMethod(context)) {
            finishTestMethodCycle();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        logger.debug("{} afterAll {}",
                identityHashCode(this), getTestClassName(context));
        if (isLastTestClassInHierarchyWithLifecyclePerClass(context)) {
            finishTestMethodCycle();
        }
        finishTestClassCycle(context);
    }

    private void startTestClassCycleIf(ExtensionContext context, Lifecycle lifecycle) {
        if (isLifecycle(context, lifecycle) && !isNestedTestClass(context)) {
            startTestClassCycle();
        }
    }

    private void startTestClassCycle() {
        ValueProviderFactory.startTestClassCycle();
        resetTestMethodCycleState();
    }

    private void startTestMethodCycle(ExtensionContext context) {
        if (isNestedTestClass(context)) {
            return;
        }
        finishTestMethodCycleIfNecessary();
        ValueProviderFactory.startTestMethodCycle();
        testMethodCycleState = CYCLE_STARTED;
    }

    private void finishTestClassCycle(ExtensionContext context) {
        if (isNestedTestClass(context)) {
            return;
        }
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

    private static boolean isLifecycle(ExtensionContext context, Lifecycle lifecycle) {
        return lifecycle == context.getTestInstanceLifecycle().orElse(null);
    }

    private static boolean isLastTestClassInHierarchyWithLifecyclePerClass(ExtensionContext context) {
        if (!isLifecycle(context, PER_CLASS)) {
            return false;
        }
        Set<Lifecycle> remainingLifecyclesInHierarchy = determineLifecyclesInTestClassHierarchy(context.getParent());
        return remainingLifecyclesInHierarchy.isEmpty() || containsOnlyLifecyclePerMethod(remainingLifecyclesInHierarchy);
    }

    private static Optional<Class<?>> getRootClassInHierarchyWithLifecyclePerClass(ExtensionContext startContext) {
        List<Class<?>> testClassesInHierarchyWithLifecyclePerClass = new ArrayList<>();
        traverseContextHierarchy(startContext, context ->
                addTestClassAtBeginningIfLifecyclePerClass(context, testClassesInHierarchyWithLifecyclePerClass));
        if (testClassesInHierarchyWithLifecyclePerClass.isEmpty()) {
            return empty();
        }
        return Optional.of(testClassesInHierarchyWithLifecyclePerClass.get(0));
    }

    private static void addTestClassAtBeginningIfLifecyclePerClass(ExtensionContext context, List<Class<?>> addTo) {
        if (!isLifecycle(context, PER_CLASS)) {
            return;
        }
        context.getTestClass().ifPresent(testClass ->
                addTo.add(0, testClass));
    }

    private static boolean testClassHierarchyHasOnlyLifecyclePerMethod(ExtensionContext context) {
        Set<Lifecycle> lifecyclesInHierarchy = determineLifecyclesInTestClassHierarchy(Optional.of(context));
        return containsOnlyLifecyclePerMethod(lifecyclesInHierarchy);
    }

    private static boolean containsOnlyLifecyclePerMethod(Set<Lifecycle> lifecyclesInHierarchy) {
        return lifecyclesInHierarchy.size() == 1 && lifecyclesInHierarchy.contains(PER_METHOD);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Set<Lifecycle> determineLifecyclesInTestClassHierarchy(Optional<ExtensionContext> optionalContext) {
        Set<Lifecycle> lifecycles = new HashSet<>();
        traverseContextHierarchy(optionalContext,
                context -> {
                    Optional<Lifecycle> lifecycle = context.getTestInstanceLifecycle();
                    lifecycle.ifPresent(lifecycles::add);
                }
        );
        return lifecycles;
    }

    private static boolean isNestedTestClass(ExtensionContext context) {
        return determineNumTestClassesInHierarchy(context) > 1;
    }

    /**
     * Cannot check e.g. for context class being org.junit.jupiter.engine.descriptor.ClassExtensionContext,
     * as this class is package private. Only ClassExtensionContext instances seem to have a non-empty
     * {@link Lifecycle} (tested via debugger for JUnit 5.10.2), however, so this is used as criteria instead.
     * This seems reasonable, as the {@link Lifecycle} is exactly what controls instantiation of test classes.
     */
    private static int determineNumTestClassesInHierarchy(ExtensionContext startContext) {
        List<ExtensionContext> contextsWithLifecycle = new ArrayList<>();
        traverseContextHierarchy(startContext,
                context -> {
                    boolean hasLifecycle = context.getTestInstanceLifecycle().isPresent();
                    if (hasLifecycle) {
                        contextsWithLifecycle.add(context);
                    }
                }
        );
        return contextsWithLifecycle.size();
    }

    private static void traverseContextHierarchy(ExtensionContext startContext,
                                                 Consumer<ExtensionContext> contextConsumer) {
        traverseContextHierarchy(Optional.of(startContext), contextConsumer);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void traverseContextHierarchy(Optional<ExtensionContext> optionalContext,
                                                 Consumer<ExtensionContext> contextConsumer) {
        while (optionalContext.isPresent()) {
            ExtensionContext context = optionalContext.get();
            contextConsumer.accept(context);
            optionalContext = context.getParent();
        }
    }
}
