package it.polimi.webapp.beans;

import java.time.LocalDateTime;

public record Offer(
        int offerId,
        int userId,
        int auctionId,
        double price,
        String name,
        LocalDateTime date
) {
}
