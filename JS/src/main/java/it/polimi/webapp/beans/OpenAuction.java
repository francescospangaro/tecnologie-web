package it.polimi.webapp.beans;


import java.util.ArrayList;
import java.util.List;

public record OpenAuction(Auction base,
                          List<Offer> offers
) implements ExtendedAuction {

    @Override
    public boolean isClosed() {
        return false;
    }
}
