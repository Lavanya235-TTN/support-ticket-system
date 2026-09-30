package com.supportticket.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketPriorityTest {

    @Test
    void values_matchesSpecPriorityEnum() {
        assertThat(TicketPriority.values())
                .containsExactly(TicketPriority.LOW, TicketPriority.MEDIUM, TicketPriority.HIGH, TicketPriority.CRITICAL);
    }
}
