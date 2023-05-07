package it.polimi.webapp.dao;

import it.polimi.webapp.beans.*;

import java.sql.*;

public class OffersDao {
    private final Connection connection;

    public OffersDao(Connection connection) {
        this.connection = connection;
    }

    public int insertOffer(Offer offer) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement getOffers = connection.prepareStatement("""
                            SELECT asta.rialzoMin, o.prezzoOfferto
                            FROM asta, offerta as o
                            WHERE asta.idAsta = ?
                            AND o.asta_idAsta = asta.idAsta
                            AND o.prezzoOfferto IN (
                                SELECT MAX(o1.prezzoOfferto)
                                FROM offerta as o1
                                WHERE o1.idOfferta = o.idOfferta
                            )
                """)) {
            getOffers.setInt(1, offer.auctionId());
            try (var result = getOffers.executeQuery()) {
                if (result.next()) {
                    if (offer.price() - result.getDouble(2) < result.getDouble(1)) {
                        connection.rollback();
                        return 0;
                    }
                }
            }
        }

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
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
