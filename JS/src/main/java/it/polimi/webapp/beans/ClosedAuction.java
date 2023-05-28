package it.polimi.webapp.beans;

public record ClosedAuction(String kind,
                            Auction base,
                            double finalPrice,
                            String buyerName,
                            String buyerAddress) implements ExtendedAuction {

    public ClosedAuction(Auction base,
                         double finalPrice,
                         String buyerName,
                         String buyerAddress) {
        this("closed", base, finalPrice, buyerName, buyerAddress);
    }

    public ClosedAuction withBase(Auction base) {
        return new ClosedAuction(base, finalPrice, buyerName, buyerAddress);
    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
