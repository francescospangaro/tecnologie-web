package it.polimi.webapp.beans;

public record ClosedAuction(Auction base,
                            double finalPrice,
                            String buyerName,
                            String buyerAddress) implements ExtendedAuction {

    @Override
    public boolean isClosed() {
        return true;
    }
}
