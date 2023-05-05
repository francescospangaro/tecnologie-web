package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public record Auction(
        int id,
        LocalDateTime expiry,
        @Nullable List<Article> articles,
        double minimumOfferDifference,
        double maxOffer
) {

    public Auction(LocalDateTime expiry, List<Article> articles, double minimumOfferDifference) {
        this(-1, expiry, articles, minimumOfferDifference, -1);
    }

    public Auction(int id, LocalDateTime expiry, double minimumOfferDifference) {
        this(id, expiry, null, minimumOfferDifference, -1);
    }

    @Override
    public int id() {
        if(id == -1)
            throw new IllegalStateException("Auction was created without id");
        return id;
    }

    @Override
    public List<Article> articles() {
        if(articles == null)
            throw new IllegalStateException("Auction was created without articles");
        return articles;
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

    public Auction withId(int id) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }

    public Auction withArticles(List<Article> articles) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }
}