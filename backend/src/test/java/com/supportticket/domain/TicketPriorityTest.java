package com.supportticket.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketPriorityTest {

    @Test
    void values_matchesSpecPriorityEnum() {
        assertThat(TicketPriority.values())
                .containsExactly(TicketPriority.LOW, TicketPriority.MEDIUM, TicketPriority.HIGH, TicketPriority.CRITICAL);
    }

    @Test
    void valueOf_parsesEachConstant() {
        for (TicketPriority priority : TicketPriority.values()) {
            assertThat(TicketPriority.valueOf(priority.name())).isEqualTo(priority);
        }
    }
}
