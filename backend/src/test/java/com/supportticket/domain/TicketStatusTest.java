package com.supportticket.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit matrix for {@code spec/state-machine.md} (NFR-07, FR-09).
 */
class TicketStatusTest {

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @MethodSource("transitionMatrix")
    void canTransitionTo_fromTo_matchesStateMachineMatrix(
            TicketStatus from, TicketStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("allStatusPairs")
    void canTransitionTo_agreesWithAllowedTargets(TicketStatus from, TicketStatus to) {
        assertThat(from.canTransitionTo(to)).isEqualTo(from.allowedTargets().contains(to));
    }

    @Test
    void canTransitionTo_nullTarget_throws() {
        assertThatThrownBy(() -> TicketStatus.OPEN.canTransitionTo(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("target status must not be null");
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("allowedTargetsByStatus")
    void allowedTargets_matchesStateMachineAndIsUnmodifiable(
            TicketStatus status, Set<TicketStatus> expected) {
        Set<TicketStatus> targets = status.allowedTargets();
        assertThat(targets).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(targets).isUnmodifiable();
    }

    static Stream<Arguments> allStatusPairs() {
        return Arrays.stream(TicketStatus.values())
                .flatMap(from -> Arrays.stream(TicketStatus.values()).map(to -> Arguments.of(from, to)));
    }

    static Stream<Arguments> allowedTargetsByStatus() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED)),
                Arguments.of(TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED)),
                Arguments.of(TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED)),
                Arguments.of(TicketStatus.CLOSED, Set.of()),
                Arguments.of(TicketStatus.CANCELLED, Set.of()));
    }

    static Stream<Arguments> transitionMatrix() {
        return Stream.of(
                row(TicketStatus.OPEN, TicketStatus.OPEN, false),
                row(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, true),
                row(TicketStatus.OPEN, TicketStatus.RESOLVED, false),
                row(TicketStatus.OPEN, TicketStatus.CLOSED, false),
                row(TicketStatus.OPEN, TicketStatus.CANCELLED, true),

                row(TicketStatus.IN_PROGRESS, TicketStatus.OPEN, false),
                row(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS, false),
                row(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, true),
                row(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED, false),
                row(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED, true),

                row(TicketStatus.RESOLVED, TicketStatus.OPEN, false),
                row(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS, false),
                row(TicketStatus.RESOLVED, TicketStatus.RESOLVED, false),
                row(TicketStatus.RESOLVED, TicketStatus.CLOSED, true),
                row(TicketStatus.RESOLVED, TicketStatus.CANCELLED, false),

                row(TicketStatus.CLOSED, TicketStatus.OPEN, false),
                row(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, false),
                row(TicketStatus.CLOSED, TicketStatus.RESOLVED, false),
                row(TicketStatus.CLOSED, TicketStatus.CLOSED, false),
                row(TicketStatus.CLOSED, TicketStatus.CANCELLED, false),

                row(TicketStatus.CANCELLED, TicketStatus.OPEN, false),
                row(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS, false),
                row(TicketStatus.CANCELLED, TicketStatus.RESOLVED, false),
                row(TicketStatus.CANCELLED, TicketStatus.CLOSED, false),
                row(TicketStatus.CANCELLED, TicketStatus.CANCELLED, false));
    }

    private static Arguments row(TicketStatus from, TicketStatus to, boolean allowed) {
        return Arguments.of(from, to, allowed);
    }
}
