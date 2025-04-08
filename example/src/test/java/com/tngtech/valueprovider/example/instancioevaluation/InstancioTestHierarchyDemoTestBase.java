package com.tngtech.valueprovider.example.instancioevaluation;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.createNumber;

@ExtendWith(InstancioExtension.class)
class InstancioTestHierarchyDemoTestBase {
    static final NumberWrapper baseClassRandom = createNumber();

    static NumberWrapper baseBeforeAllRandom;

    NumberWrapper baseInstanceRandom = createNumber();

    NumberWrapper baseBeforeEachRandom;

    @BeforeAll
    static void baseBeforeAll() {
        baseBeforeAllRandom = createNumber();
    }

    @BeforeEach
    void baseBeforeEach() {
        baseBeforeEachRandom = createNumber();
    }
}