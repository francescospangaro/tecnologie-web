package it.polimi.webapp.beans;

import java.time.LocalDate;

public record Offer(
        int userId,
        int auctionId,
        double price,
        String name,
        LocalDate date
) {
}
