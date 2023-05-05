package it.polimi.webapp.dao;

import it.polimi.webapp.beans.*;

import java.sql.*;

public class OffersDao {
    private final Connection connection;

    public OffersDao(Connection connection) {
        this.connection = connection;
    }

    public int insertOffer(Offer offer) throws SQLException {
//        TODO: check why it inserts an offer only if the offer table is already populated
        connection.setAutoCommit(false);
        try (PreparedStatement getOffers = connection.prepareStatement("""
                            SELECT offerta.prezzoOfferto
                            FROM offerta
                            WHERE offerta.prezzoOfferto >= ?
                            AND offerta.asta_idAsta = ?
                """)) {
            getOffers.setDouble(1, offer.price());
            getOffers.setInt(2, offer.auctionId());
            try (var result = getOffers.executeQuery()) {
                if (result.next()) {
                    connection.rollback();
                    return 0;
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
