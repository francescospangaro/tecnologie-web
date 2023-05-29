package it.polimi.webapp.dao;

import it.polimi.webapp.Transactions;
import it.polimi.webapp.beans.*;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Clock;
import java.time.LocalDateTime;

public class OffersDao {
    private final Connection connection;
    private final Clock clock;

    public OffersDao(Connection connection) {
        this(connection, Clock.systemDefaultZone());
    }

    public OffersDao(Connection connection, Clock clock) {
        this.connection = connection;
        this.clock = clock;
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
    public InsertionReturn insertOffer(Offer offer) throws SQLException {
        var isolationLevel = connection.getTransactionIsolation();
        try {
            return Transactions.start(connection, Transactions.Type.NESTED, tx -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                var id = doInsertOffer(tx, offer);
                return new InsertionReturn(TypeError.DONE, id);
            });
        } catch (InsertionException ex) {
            return new InsertionReturn(ex.result, null);
        } finally {
            connection.setTransactionIsolation(isolationLevel);
        }
    }

    private int doInsertOffer(Connection tx, Offer offer) throws SQLException, InsertionException {
        // Touch with an update all items of this auction, so that we will hold a transaction lock on those
        // If anybody else tries to do the same thing (so basically this method is called concurrently)
        // he will have to wait for us to be done with our transaction
        try (PreparedStatement lockItems = tx.prepareStatement("""
                UPDATE astearticoli
                SET astearticoli.articolo_codArticolo = astearticoli.articolo_codArticolo
                WHERE astearticoli.asta_idAsta = ?
                """)) {
            lockItems.setInt(1, offer.auctionId());
            lockItems.executeUpdate();
        }

        try (PreparedStatement checkClosedOrExpired = tx.prepareStatement("""
                SELECT asta.idAsta
                FROM asta
                WHERE asta.idAsta = ?
                AND (asta.chiusa = 1 OR asta.scadenza <= ?)
                """)) {
            checkClosedOrExpired.setInt(1, offer.auctionId());
            checkClosedOrExpired.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now(clock)));

            try (var result = checkClosedOrExpired.executeQuery()) {
                if (result.next()) {
                    // The buyer is trying to put an offer in a closed auction
                    // The buyer is trying to put an offer in an expired auction
                    throw new InsertionException(TypeError.DB_ERROR);
                }
            }
        }

        boolean foundElement;
        try (PreparedStatement firstCheck = tx.prepareStatement("""
                SELECT asta.rialzoMin, o.prezzoOfferto
                FROM asta
                     JOIN offerta as o ON asta.idAsta = o.asta_idAsta
                WHERE asta.idAsta = ?
                AND o.prezzoOfferto IN (
                    SELECT MAX(o1.prezzoOfferto)
                    FROM offerta as o1
                    WHERE o1.asta_idAsta = o.asta_idAsta
                )
                """)) {
            firstCheck.setInt(1, offer.auctionId());
            try (var result = firstCheck.executeQuery()) {
                foundElement = result.next();
                if (foundElement && offer.price() - result.getDouble(2) < result.getDouble(1)) {
                    //The next offer is not high enough to surpass the minPriceIncrease threshold
                    throw new InsertionException(TypeError.LOWER_THAN_MAX);
                }
            }
        }

        if (!foundElement) {
            try (PreparedStatement secondCheck = tx.prepareStatement("""
                    SELECT SUM(articolo.prezzo)
                    FROM articolo
                         JOIN astearticoli as a ON articolo.codArticolo = a.articolo_codArticolo
                    WHERE a.asta_idAsta = ?
                    """)) {
                secondCheck.setInt(1, offer.auctionId());

                try (var res = secondCheck.executeQuery()) {
                    if (!res.next()) {
                        //DB error
                        throw new InsertionException(TypeError.DB_ERROR);
                    }

                    if (offer.price() < res.getDouble(1)) {
                        //The offer is not higher than the sum of the articles' value
                        //The buyer is trying to put an offer in a closed auction
                        //The buyer is trying to put an offer in an expired auction
                        throw new InsertionException(TypeError.LOWER_THAN_ARTICLE);
                    }
                }
            }
        }

        // new offer is inserted
        try (PreparedStatement insertOffer = tx.prepareStatement("""
              INSERT INTO offerta (prezzoOfferto, dataOfferta, utente_idUtente, asta_idAsta)
              VALUES (?, ?, ?, ?)
              """, Statement.RETURN_GENERATED_KEYS)) {
            insertOffer.setDouble(1, offer.price());
            insertOffer.setTimestamp(2, Timestamp.valueOf(offer.date()));
            insertOffer.setInt(3, offer.userId());
            insertOffer.setInt(4, offer.auctionId());
            int res = insertOffer.executeUpdate();
            if (res == 0)
                throw new InsertionException(TypeError.DB_ERROR);

            try (var generatedKeys = insertOffer.getGeneratedKeys()) {
                if (!generatedKeys.next())
                    throw new SQLException("Creating offer failed, no ID obtained.");
                return generatedKeys.getInt(1);
            }
        }
    }

    private static class InsertionException extends Exception {

        private final TypeError result;

        public InsertionException(TypeError result) {
            this.result = result;
        }
    }

    public enum TypeError {
        LOWER_THAN_MAX, LOWER_THAN_ARTICLE, DB_ERROR, DONE
    }

    public record InsertionReturn(TypeError type, @Nullable Integer id) {
        public TypeError type() {
            return type;
        }

        @Override
        public @Nullable Integer id() {
            return id;
        }
    }
}
