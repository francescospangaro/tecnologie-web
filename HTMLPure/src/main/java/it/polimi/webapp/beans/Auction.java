package it.polimi.webapp.beans;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record Auction(
        int id,
        LocalDateTime expiry,
        List<Article> articles,
        int minimumOfferDifference,
        double maxOffer
) {

    public Auction(int id,
                   LocalDateTime expiry,
                   List<Article> articles,
                   int minimumOfferDifference) {
        this(id, expiry, articles, minimumOfferDifference, -1);
    }

    @Override
    public double maxOffer() {
        if(maxOffer == -1D)
            throw new IllegalStateException("Auction was created without maxOffer");
        return maxOffer;
    }

    /** returns the number of days between login and expiration date */
    public long getRemainingDays(LocalDateTime now) {
        return now.isAfter(expiry) ? 0 : Duration.between(now, expiry).toDays();
    }

    /** returns the number of hours between login and expiration date (< 24) */
    public int getRemainingHours(LocalDateTime now) {
        return now.isAfter(expiry) ? 0 :  Duration.between(now, expiry).toHoursPart();
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiry);
    }

    public Auction withArticles(List<Article> articles) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }
}