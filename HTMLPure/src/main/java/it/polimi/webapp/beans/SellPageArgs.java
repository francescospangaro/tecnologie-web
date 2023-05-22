package it.polimi.webapp.beans;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record SellPageArgs(InsertionState articleInsertion,
                           ArticleData article,
                           InsertionState auctionInsertion,
                           AuctionData auction) {

    public SellPageArgs() {
        this(InsertionState.NONE, new ArticleData(), InsertionState.NONE, new AuctionData());
    }

    public SellPageArgs(InsertionState articleInsertion, ArticleData article) {
        this(articleInsertion, article, InsertionState.NONE, new AuctionData());
    }

    public SellPageArgs(InsertionState auctionInsertion, AuctionData auction) {
        this(InsertionState.NONE, new ArticleData(), auctionInsertion, auction);
    }

    public record ArticleData(
            String name,
            String desc,
            @Nullable Double price) {

        public ArticleData() {
            this("", "", null);
        }
    }

    public record AuctionData(
            @Nullable Integer minimumOfferDifference,
            @Nullable LocalDateTime expiry,
            List<Integer> articleIds) {

        public AuctionData() {
            this(null, null, List.of());
        }
    }

    public enum InsertionState {
        ERROR_DATA_FORMAT,
        ERROR_QUERY,
        SUCCESS,
        NONE;

        public boolean isDataFormatError() {
            return this == ERROR_DATA_FORMAT;
        }

        public boolean isQueryError() {
            return this == ERROR_QUERY;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
}