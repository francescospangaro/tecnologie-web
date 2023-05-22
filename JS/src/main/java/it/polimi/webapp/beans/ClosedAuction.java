package it.polimi.webapp.beans;

public record ClosedAuction(Auction base,
                            double finalPrice,
                            String buyerName,
                            String buyerAddress) implements ExtendedAuction {

    public ClosedAuction withBase(Auction base) {
        return new ClosedAuction(base, finalPrice, buyerName, buyerAddress);
    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
