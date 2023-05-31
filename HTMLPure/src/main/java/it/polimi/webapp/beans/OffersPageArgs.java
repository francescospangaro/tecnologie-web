package it.polimi.webapp.beans;

import it.polimi.webapp.dao.OffersDao;
import org.jetbrains.annotations.Nullable;

public record OffersPageArgs(@Nullable Integer auctionId, @Nullable Double offerPrice, OffersDao.InsertionResult offerInsertion) {

    public OffersPageArgs() {
        this(null, null, OffersDao.InsertionResult.DONE);
    }
}