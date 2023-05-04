package it.polimi.webapp.beans;

import java.time.LocalDate;

public record Offer(
        int userId,
        int auctionId,
        double price,
        LocalDate date
) {
}
