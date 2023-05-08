package it.polimi.webapp.dao;

import it.polimi.webapp.beans.*;

import java.sql.*;

public class OffersDao {
    private final Connection connection;

    public OffersDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * First query checks if the incoming offer is
     * a higher price than the max registered for the same auction
     * and checks if the difference in price is >= than minPriceIncrease
     * <p>
     * Second query checks is only run if the first one returns nothing,
     * meaning that for that auction there are no registered offers, then checks
     * if the incoming offer is >= than the sum of the articles' prices for that auction
     */
    public int insertOffer(Offer offer) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement firstCheck = connection.prepareStatement("""
                            SELECT asta.rialzoMin, o.prezzoOfferto
                            FROM asta, offerta as o
                            WHERE asta.idAsta = ?
                            AND o.asta_idAsta = asta.idAsta
                            AND o.prezzoOfferto IN (
                                SELECT MAX(o1.prezzoOfferto)
                                FROM offerta as o1
                                WHERE o1.asta_idAsta = o.asta_idAsta
                            )
                """)) {
            firstCheck.setInt(1, offer.auctionId());
            try (var result = firstCheck.executeQuery()) {
                if (result.next()) {
                    if (offer.price() - result.getDouble(2) < result.getDouble(1)) {
                        //The next offer is not high enough to surpass the minPriceIncrease threshold
                        connection.rollback();
                        System.out.println("Magia1");
                        return -2;
                    }
                    // no error, goes on without interruption
                } else {
                    try (PreparedStatement secondCheck = connection.prepareStatement("""
                                        SELECT SUM(articolo.prezzo)
                                        FROM articolo, astearticoli as a
                                        WHERE articolo.codArticolo = a.articolo_codArticolo
                                        AND a.asta_idAsta = ?
                            """)) {
                        secondCheck.setInt(1, offer.auctionId());
                        try (var res = secondCheck.executeQuery()) {
                            if (res.next()) {
                                if (offer.price() < res.getDouble(1)) {
                                    //The offer is not higher than the sum of the articles' value
                                    connection.rollback();
                                    System.out.println("Magia2");
                                    return -1;
                                }
                                // no error, goes on without interruption
                            } else {
                                //DB error
                                connection.rollback();
                                System.out.println("Magia3");
                                return 0;
                            }
                        }
                    }
                }
            }
        }

        // new offer is inserted
        try (PreparedStatement insertOffer = connection.prepareStatement(
                "INSERT INTO offerta (prezzoOfferto, dataOfferta, utente_idUtente, asta_idAsta) VALUES (?, ?, ?, ?)")) {
            insertOffer.setDouble(1, offer.price());
            insertOffer.setTimestamp(2, Timestamp.valueOf(offer.date()));
            insertOffer.setInt(3, offer.userId());
            insertOffer.setInt(4, offer.auctionId());
            int res = insertOffer.executeUpdate();
            if (res == 0)
                return 0;
            connection.commit();
            return 1;
        } catch (Throwable t) {
            // DB error
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
