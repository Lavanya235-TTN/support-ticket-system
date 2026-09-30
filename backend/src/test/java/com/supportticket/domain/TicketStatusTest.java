package com.supportticket.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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
