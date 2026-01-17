package com.tngtech.valueprovider;

import java.util.*;

import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.CollectionGenerator.MapEntry.entryOf;
import static com.tngtech.valueprovider.CollectionGeneratorTest.MyStringTestDataFactory.BASE_PROPERTY_NAME;
import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionGeneratorTest {
    private static final int DEFAULT_MIN_COLLECTION_SIZE = 0;
    static final int DEFAULT_MAX_COLLECTION_SIZE = 5;

    private final ValueProvider random = withRandomValues();
    private final CollectionGenerator<ValueProvider> collection = random.collection();

    @Test
    void should_create_List_of_default_size_range() {
        CollectionGenerator<ValueProvider> collection = random.collection();

        List<Integer> list = collection.listOf(vp -> 1);

        assertThat(list).hasSizeGreaterThanOrEqualTo(DEFAULT_MIN_COLLECTION_SIZE)
                .hasSizeLessThanOrEqualTo(DEFAULT_MAX_COLLECTION_SIZE);
    }

    @Test
    void should_create_List_of_specified_size() {
        int specifiedSize = random.intNumber(10, 42);
        List<Integer> expected = new ArrayList<>();

        List<Integer> list = collection
                .numElements(specifiedSize)
                .listOf(vp -> {
                    int element = vp.positiveIntNumber();
                    expected.add(element);
                    return element;
                });

        assertThat(list).hasSize(specifiedSize)
                .isEqualTo(expected);
    }

    @Test
    void should_create_Set_of_specified_size() {
        int specifiedSize = random.intNumber(11, 43);
        Set<Integer> expected = new HashSet<>();

        Set<Integer> set = collection
                .numElements(specifiedSize)
                .setOf(vp -> {
                    int element = vp.positiveIntNumber();
                    expected.add(element);
                    return element;
                });

        assertThat(set).hasSize(specifiedSize)
                .isEqualTo(expected);
    }

    @Test
    void should_create_Map_of_specified_size() {
        int specifiedSize = random.intNumber(12, 44);
        Map<String, Integer> expected = new HashMap<>();

        Map<String, Integer> map = collection
                .numElements(specifiedSize)
                .replacePrefixVia(i -> format("%d-", (i + 1)))
                .mapOf(vp -> {
                    String key = vp.fixedDecoratedString("key");
                    int value = vp.positiveIntNumber();
                    expected.put(key, value);
                    return entryOf(key, value);
                });

        assertThat(map).hasSize(specifiedSize)
                .isEqualTo(expected);
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

        Set<MyString> set = collection
                .appendToPrefixVia(i -> format("%c_", (char) ('a' + i)))
                .setOf(MyStringTestDataFactory::createMyString);

        List<String> strings = extractStrings(set);
        assertThat(strings).containsExactlyInAnyOrder(
                prefixToBeAppendedTo + "a_" + suffixedPropertyName,
                prefixToBeAppendedTo + "b_" + suffixedPropertyName,
                prefixToBeAppendedTo + "c_" + suffixedPropertyName);
    }

    @Test
    void numElements_should_fail_for_negative_values() {
        int illegalNegative = random.intNumber(-1000, -1);

        assertThatThrownBy(() -> collection
                .numElements(illegalNegative))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("" + illegalNegative);
    }

    @Test
    void setOf_should_fail_when_creating_specified_number_of_different_elements_is_not_possible() {
        int specifiedSize = random.intNumber(3, 5);
        CollectionGenerator<ValueProvider> generator = collection
                .numElements(specifiedSize);

        assertThatThrownBy(() -> generator
                .setOf(vp -> 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("" + specifiedSize);
    }

    @Test
    void entryOf_should_fail_for_null_value_for_key() {
        String value = random.fixedDecoratedString("value");

        assertThatThrownBy(() -> entryOf(null, value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("key");
    }

    @Test
    void mapOf_should_fail_when_creating_specified_number_of_different_keys_is_not_possible() {
        int specifiedSize = random.intNumber(3, 5);
        CollectionGenerator<ValueProvider> generator = collection
                .numElements(specifiedSize)
                .replacePrefixVia(i -> format("%d-", (i + 1)));

        assertThatThrownBy(() -> generator
                .mapOf(vp -> entryOf(1, vp.fixedDecoratedString("value"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("" + specifiedSize);
    }

    @Value
    private static class MyString {
        String value;
    }

    private static List<String> extractStrings(Collection<MyString> collection) {
        return collection.stream()
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