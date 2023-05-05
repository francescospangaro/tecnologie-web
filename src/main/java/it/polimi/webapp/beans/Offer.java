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

    public Offer(int userId, int auctionId, double price, LocalDateTime dateTime){
        this(-1, userId, auctionId, price, "", dateTime);
    }

}
