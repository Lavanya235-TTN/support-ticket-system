package com.supportticket.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TARGETS = Map.of(
            OPEN, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(RESOLVED, CANCELLED),
            RESOLVED, Set.of(CLOSED),
            CLOSED, Set.of(),
            CANCELLED, Set.of());

    public boolean canTransitionTo(TicketStatus target) {
        Objects.requireNonNull(target, "target status must not be null");
        return ALLOWED_TARGETS.get(this).contains(target);
    }

    public Set<TicketStatus> allowedTargets() {
        return Set.copyOf(ALLOWED_TARGETS.get(this));
    }
}
