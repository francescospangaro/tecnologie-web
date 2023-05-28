package it.polimi.webapp.beans;


import java.util.List;

public record OpenAuction(String kind,
                          Auction base,
                          List<Offer> offers
) implements ExtendedAuction {

    public OpenAuction(Auction base, List<Offer> offers) {
        this("open", base, offers);
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
