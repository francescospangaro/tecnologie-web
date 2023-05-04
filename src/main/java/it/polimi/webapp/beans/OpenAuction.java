package it.polimi.webapp.beans;

public record OpenAuction(Auction base,
                          double finalPrice,
                          String buyerName,
                          String buyerAddress) implements ExtendedAuction {

    @Override
    public boolean isClosed() {
        return false;
    }
}
