package com.supportticket.domain;

import java.util.Collections;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean canTransitionTo(TicketStatus target) {
        return false;
    }

    public Set<TicketStatus> allowedTargets() {
        return Collections.emptySet();
    }
}
