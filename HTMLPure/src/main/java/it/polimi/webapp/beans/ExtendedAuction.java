package it.polimi.webapp.beans;

public interface ExtendedAuction {

    Auction base();

    boolean isClosed();

    default boolean isOpen() {
        return !isClosed();
    }
}
