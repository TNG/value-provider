package com.tngtech.valueprovider;

import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.valueprovider.CollectionGeneratorTest.MyStringTestDataFactory.BASE_PROPERTY_NAME;
import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

class CollectionGeneratorTest {
    private static final int DEFAULT_MIN_COLLECTION_SIZE = 0;
    private static final int DEFAULT_MAX_COLLECTION_SIZE = 5;

    private final ValueProvider random = withRandomValues();
    private final CollectionGenerator<ValueProvider> collection = random.collection();

    @Test
    void should_create_lists_of_default_size() {
        CollectionGenerator<ValueProvider> collection = random.collection();

        List<Integer> list = collection.listOf(vp -> 1);

        assertThat(list).hasSizeGreaterThanOrEqualTo(DEFAULT_MIN_COLLECTION_SIZE)
                .hasSizeLessThanOrEqualTo(DEFAULT_MAX_COLLECTION_SIZE);
    }

    @Test
    void should_create_List_of_specified_size() {
        int specifiedSize = random.intNumber(10, 42);

        List<Integer> list = collection
                .numElements(specifiedSize)
                .listOf(vp -> 1);

        assertThat(list).hasSize(specifiedSize);
    }

    @Test
    void should_create_List_of_specified_size_range() {
        CollectionGenerator<ValueProvider> collection = random.collection();
        int minSpecifiedSize = random.intNumber(1, 5);
        int maxSpecifiedSize = minSpecifiedSize + random.intNumber(0, 4);

        List<Integer> list = collection
                .numElements(minSpecifiedSize, maxSpecifiedSize)
                .listOf(vp -> 1);

        assertThat(list).hasSizeGreaterThanOrEqualTo(minSpecifiedSize)
                .hasSizeLessThanOrEqualTo(maxSpecifiedSize);
    }

    @Test
    void should_support_replacing_the_prefix() {
        String prefixToBeReplaced = "to be replaced";
        ValueProvider prefixedRandom = random.copyWithChangedPrefix(prefixToBeReplaced);
        String suffixedPropertyName = BASE_PROPERTY_NAME + prefixedRandom.getSuffix();
        CollectionGenerator<ValueProvider> collection = prefixedRandom.collection().numElements(3);

        List<MyString> list = collection
                .replacePrefixVia(i -> format("%d-", (i + 1)))
                .listOf(MyStringTestDataFactory::createMyString);

        List<String> strings = extractStrings(list);
        assertThat(strings).containsExactly(
                "1-" + suffixedPropertyName,
                "2-" + suffixedPropertyName,
                "3-" + suffixedPropertyName);
    }

    @Test
    void should_support_appending_to_the_prefix() {
        String prefixToBeAppendedTo = "A_";
        ValueProvider prefixedRandom = random.copyWithChangedPrefix(prefixToBeAppendedTo);
        String suffixedPropertyName = BASE_PROPERTY_NAME + prefixedRandom.getSuffix();
        CollectionGenerator<ValueProvider> collection = prefixedRandom.collection().numElements(3);

        List<MyString> list = collection
                .appendToPrefixVia(i -> format("%c_", (char) ('a' + i)))
                .listOf(MyStringTestDataFactory::createMyString);

        List<String> strings = extractStrings(list);
        assertThat(strings).containsExactly(
                prefixToBeAppendedTo + "a_" + suffixedPropertyName,
                prefixToBeAppendedTo + "b_" + suffixedPropertyName,
                prefixToBeAppendedTo + "c_" + suffixedPropertyName);
    }

    @Value
    private static class MyString {
        String value;
    }

    private static List<String> extractStrings(List<MyString> list) {
        return list.stream()
                .map(MyString::getValue)
                .collect(toList());
    }

    @NoArgsConstructor(access = PRIVATE)
    static class MyStringTestDataFactory {
        static final String BASE_PROPERTY_NAME = "value";

        static MyString createMyString(ValueProvider values) {
            return new MyString(values.fixedDecoratedString(BASE_PROPERTY_NAME));
        }
    }

    private static ValueProvider withRandomValues() {
        return builderWithRandomValues().build();
    }

    private static ValueProvider.Builder builderWithRandomValues() {
        return new ValueProvider.Builder(createRandomInitialization());
    }
}