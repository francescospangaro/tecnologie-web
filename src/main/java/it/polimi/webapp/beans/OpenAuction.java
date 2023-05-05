package it.polimi.webapp.beans;


import java.util.ArrayList;
import java.util.List;

public record OpenAuction(Auction base,
                          List<Offer> offers
) implements ExtendedAuction {

    public OpenAuction(){
        this(new Auction(),new ArrayList<>());
    }
    @Override
    public boolean isClosed() {
        return false;
    }
}
