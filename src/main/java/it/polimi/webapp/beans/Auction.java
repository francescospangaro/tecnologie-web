package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

public record Auction(
        int id,
        LocalDate expiry,
        @Nullable List<Article> articles,
        double minimumOfferDifference
) {

    public Auction(LocalDate expiry, List<Article> articles, double minimumOfferDifference) {
        this(-1, expiry, articles, minimumOfferDifference);
    }

    public Auction(int id, LocalDate expiry, double minimumOfferDifference) {
        this(id, expiry, null, minimumOfferDifference);
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

    public Auction withId(int id) {
        return new Auction(id, expiry, articles, minimumOfferDifference);
    }

    public Auction withArticles(List<Article> articles) {
        return new Auction(id, expiry, articles, minimumOfferDifference);
    }
}