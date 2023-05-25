package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public record Auction(
        int id,
        LocalDateTime expiry,
        @Nullable List<Article> articles,
        int minimumOfferDifference,
        double maxOffer
) {

    public Auction(LocalDateTime expiry, List<Article> articles, int minimumOfferDifference) {
        this(-1, expiry, articles, minimumOfferDifference, -1);
    }

    public Auction(int id, LocalDateTime expiry, int minimumOfferDifference) {
        this(id, expiry, null, minimumOfferDifference, -1);
    }

    public Auction(Auction toCopy, double maxOffer){
        this(toCopy.id, toCopy.expiry, toCopy.articles, toCopy.minimumOfferDifference, maxOffer);
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

    public Auction withId(int id) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }

    public Auction withArticles(List<Article> articles) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }
}