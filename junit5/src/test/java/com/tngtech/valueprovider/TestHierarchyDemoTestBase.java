package com.tngtech.valueprovider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

class TestHierarchyDemoTestBase {
    static final ValueProvider baseClassRandom;

    static {
        ensureDefinedFactoryState();
        baseClassRandom = createRandomValueProvider();
    }

    static ValueProvider baseBeforeAllRandom;

    ValueProvider baseInstanceRandom = createRandomValueProvider();

    ValueProvider baseBeforeEachRandom;

    @BeforeAll
    static void baseBeforeAll() {
        baseBeforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void baseBeforeEach() {
        baseBeforeEachRandom = createRandomValueProvider();
    }
}