package com.tngtech.valueprovider;

import java.time.LocalDateTime;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.time.ZoneId.SHORT_IDS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.TimeZone.getTimeZone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InitializationCreatorSnapshotTest {
    // Europe
    private static final TimeZone TZ1 = getTimeZone(SHORT_IDS.get("ECT"));
    // Australia
    private static final TimeZone TZ2 = getTimeZone(SHORT_IDS.get("ACT"));

    private TimeZone systemTimeZone;

    @BeforeEach
    void setUp() {
        systemTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void tearDown() {
        if (systemTimeZone != null) {
            TimeZone.setDefault(systemTimeZone);
        }
    }

    @Test
    void should_return_provided_referenceDateTime_even_if_system_timezone_is_changed() {
        LocalDateTime reference = getDateTimeTruncatedToMilliseconds();
        InitializationCreatorSnapshot snapshot = new InitializationCreatorSnapshot(42L, reference, 4711L);
        changeTimezone();

        LocalDateTime restored = snapshot.getReferenceDateTime();

        assertThat(restored).isEqualTo(reference);
    }

    private void changeTimezone() {
        TimeZone changed = systemTimeZone.getRawOffset() == TZ1.getRawOffset() ? TZ2 : TZ1;
        TimeZone.setDefault(changed);
    }

    @Test
    void should_only_accept_referenceDateTime_truncated_to_milliseconds() {
        LocalDateTime truncated = getDateTimeTruncatedToMilliseconds();
        LocalDateTime notTruncated = truncated.plusNanos(500_000);

        // must succeed
        new InitializationCreatorSnapshot(42L, truncated, 4711L);

        assertThatThrownBy(() ->
                new InitializationCreatorSnapshot(42L, notTruncated, 4711L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll(notTruncated.toString(), "milliseconds");
    }

    private LocalDateTime getDateTimeTruncatedToMilliseconds() {
        return LocalDateTime.now().truncatedTo(MILLIS);
    }
}