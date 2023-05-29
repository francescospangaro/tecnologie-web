package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record Auction(
        int id,
        LocalDateTime expiry,
        List<Article> articles,
        int minimumOfferDifference,
        @Nullable Double maxOffer
) {

    public Auction(int id, LocalDateTime expiry, List<Article> articles, int minimumOfferDifference) {
        this(id, expiry, articles, minimumOfferDifference, null);
    }

    public Auction withArticles(List<Article> articles) {
        return new Auction(id, expiry, articles, minimumOfferDifference, maxOffer);
    }
}