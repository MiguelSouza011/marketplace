package io.miguelsouza.marketplace.ticketing.infrastructure.http.request;

import io.miguelsouza.marketplace.ticketing.domain.SeatId;

public record SeatSelectionRequest(String id) {
    public SeatId toInput() {
        return new SeatId(id);
    }
}
