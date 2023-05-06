package it.polimi.webapp.dao;

import it.polimi.webapp.beans.*;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionDao {
    private final Connection connection;

    public AuctionDao(Connection connection) {
        this.connection = connection;
    }

    public @Nullable List<Auction> findAuctions(int userId, boolean closed) throws SQLException {

        try (var query = connection.prepareStatement("""
                SELECT a.idAsta, a.scadenza, a.rialzoMin, articolo.codArticolo,
                    articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo, offerta.prezzoOfferto
                FROM articolo, astearticoli, utente, (asta as a LEFT OUTER JOIN offerta ON a.idAsta = offerta.asta_idAsta)
                WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                AND a.idAsta=astearticoli.asta_idAsta
                AND articolo.utente_idUtente=?
                AND a.chiusa=?
                AND (offerta.idOfferta IS NULL OR offerta.prezzoOfferto IN (
                    select MAX(offerta.prezzoOfferto)
                    from offerta, asta as a1
                    WHERE a1.idAsta = a.idAsta
                ))
                ORDER BY a.scadenza""")) {
            query.setInt(1, userId);
            query.setInt(2, closed ? 1 : 0);

            try (var res = query.executeQuery()) {
                Map<Integer, Auction> auctions = new HashMap<>();
                while (res.next()) {
                    int id = res.getInt(1);
                    var currAuction = auctions.get(id);
                    if (currAuction == null)
                        auctions.put(id, currAuction = new Auction(id,
                                res.getTimestamp(2).toLocalDateTime(),
                                new ArrayList<>(),
                                res.getDouble(3),
                                res.getDouble(9)));

                    currAuction.articles().add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            res.getString(7),
                            res.getDouble(8),
                            userId));
                }

                return List.copyOf(auctions.values());
            }
        }
    }



    public @Nullable ExtendedAuction findAuctionByIds(int userId, int auctionId) throws SQLException {
        Auction baseAuction = null;
        boolean closed = false;

        try (var query = connection.prepareStatement("""
                SELECT asta.scadenza, asta.rialzoMin, asta.chiusa, articolo.codArticolo,
                    articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo
                FROM articolo, asta, astearticoli, utente
                WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                AND asta.idAsta=astearticoli.asta_idAsta
                AND articolo.utente_idUtente=?
                AND asta_idAsta = ?""")) {
            query.setInt(1, userId);
            query.setInt(2, auctionId);

            try (var res = query.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (res.next()) {
                    if (baseAuction == null) {
                        baseAuction = new Auction(auctionId, res.getTimestamp(1).toLocalDateTime(), res.getDouble(2));
                        closed = res.getBoolean(3);
                    }

                    articles.add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            res.getString(7),
                            res.getDouble(8),
                            userId));
                }

                if (baseAuction == null)
                    return null;

                baseAuction = baseAuction.withArticles(articles);
            }
        }

        return closed
                ? doPopulateClosedAuction(baseAuction)
                : doPopulateOpenAuction(baseAuction);
    }

    public @Nullable OpenAuction findAuctionByAuctionId(int auctionId) throws SQLException {
        Auction baseAuction = null;

        try (var query = connection.prepareStatement("""
                SELECT asta.scadenza, asta.rialzoMin, articolo.codArticolo,
                    articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo
                FROM articolo, asta, astearticoli, utente
                WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                AND asta.idAsta=astearticoli.asta_idAsta
                AND asta_idAsta = ?""")) {
            query.setInt(1, auctionId);

            try (var res = query.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (res.next()) {
                    if (baseAuction == null) {
                        baseAuction = new Auction(auctionId, res.getTimestamp(1).toLocalDateTime(), res.getDouble(2));
                    }

                    articles.add(new Article(
                            res.getInt(3),
                            res.getString(4),
                            res.getString(5),
                            res.getString(6),
                            res.getDouble(7),
                            -1));
                }

                if (baseAuction == null)
                    return null;

                baseAuction = baseAuction.withArticles(articles);
            }
        }

        return doPopulateOpenAuction(baseAuction);
    }

    private @Nullable ClosedAuction doPopulateClosedAuction(Auction base) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT offerta.prezzoOfferto, utente.nome, utente.indirizzo
                FROM utente, offerta, asta as a
                WHERE offerta.asta_idAsta = ?
                AND offerta.utente_idUtente = utente.idUtente
                AND a.chiusa = true
                AND offerta.prezzoOfferto IN (
                        select MAX(offerta.prezzoOfferto)
                        from offerta, asta as a1
                        WHERE a1.idAsta = a.idAsta
                    )
                """)) {
            query.setInt(1, base.id());
            try (var res = query.executeQuery()) {
                if (res.next())
                    return new ClosedAuction(base, res.getDouble(1), res.getString(2), res.getString(3));
                return new ClosedAuction(base, 0, "none", "");
            }
        }
    }

    private @Nullable OpenAuction doPopulateOpenAuction(Auction base) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT offerta.idOfferta, offerta.utente_idUtente, offerta.asta_idAsta, offerta.prezzoOfferto, utente.nome, offerta.dataOfferta
                FROM utente, offerta, asta as a
                WHERE offerta.asta_idAsta = ?
                AND offerta.utente_idUtente = utente.idUtente
                AND a.chiusa = false
                """)) {
            query.setInt(1, base.id());
            try (var res = query.executeQuery()) {
                List<Offer> offers = new ArrayList<>();
                while (res.next())
                    offers.add(new Offer(res.getInt(1), res.getInt(2), res.getInt(3), res.getDouble(4), res.getString(5), res.getTimestamp(6).toLocalDateTime()));
                return new OpenAuction(base, offers);
            }
        }
    }

    public int insertAuction(Auction auction) throws SQLException {
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement insertAuction = connection.prepareStatement(
                    "INSERT INTO asta (rialzoMin, scadenza) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertAuction.setDouble(1, auction.minimumOfferDifference());
                insertAuction.setTimestamp(2, Timestamp.valueOf(auction.expiry()));
                int res = insertAuction.executeUpdate();
                if (res == 0)
                    return 0;

                try (var generatedKeys = insertAuction.getGeneratedKeys()) {
                    if (!generatedKeys.next())
                        throw new SQLException("Creating auction failed, no ID obtained.");

                    auction = auction.withId(generatedKeys.getInt(1));
                }
            }

            var articleIds = auction.articles().stream()
                    .map(Article::codArticle)
                    .toArray(Integer[]::new);
            /*try (var query = connection.prepareStatement("""
                    SELECT a1.codArticolo
                    FROM articolo as a1, asta, astearticoli
                    WHERE a1.codArticolo in ?
                    AND a1.codArticolo = astearticoli.articolo_codArticolo
                    AND astearticoli.asta_idAsta = asta.idAsta
                    AND asta.chiusa = true
                    """)) {
                query.setArray(1, connection.createArrayOf("int", articleIds));

                try (var res = query.executeQuery()) {
                    if (res.next()) {
                        connection.rollback();
                        return 0;
                    }
                }
            }*/

            try (PreparedStatement relate = connection.prepareStatement(
                    "INSERT INTO astearticoli (articolo_codArticolo, asta_idAsta) VALUES (?, ?)")
            ) {
                for (int articleId : articleIds) {
                    relate.setInt(1, articleId);
                    relate.setInt(2, auction.id());
                    relate.addBatch();
                }

                relate.executeBatch();
            }

            connection.commit();
            return 1;
        } catch (Throwable t) {
            connection.rollback();
            throw t;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public int closeAuction(int auctionId, LocalDateTime loginTime, int userId) throws SQLException {
        try (PreparedStatement close = connection.prepareStatement("""
                UPDATE asta
                INNER JOIN (
                    SELECT a.idAsta as oldId
                    from asta as a, utente, astearticoli, articolo
                    where astearticoli.asta_idAsta = a.idAsta
                    and astearticoli.articolo_codArticolo = articolo.codArticolo
                    and articolo.utente_idUtente = ?
                ) as joinedAuctions
                ON joinedAuctions.oldId = asta.idAsta
                SET asta.chiusa = true
                WHERE asta.idAsta = ? and asta.scadenza < ?
                """)) {
            close.setInt(1, userId);
            close.setInt(2, auctionId);
            close.setTimestamp(3, Timestamp.valueOf(loginTime));
            return close.executeUpdate();
        }
    }


    public List<Auction> findAuctionByWord(String search) {
        try (var query = connection.prepareStatement("""
                SELECT asta.idAsta, asta.scadenza, asta.rialzoMin, articolo.codArticolo,
                    articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo
                FROM articolo, asta, astearticoli
                WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                AND asta.idAsta=astearticoli.asta_idAsta
                AND asta.chiusa=false
                AND asta.idAsta IN (
                    SELECT astearticoli.asta_idAsta
                    from astearticoli, articolo
                    WHERE articolo_codArticolo = articolo.codArticolo
                    AND (articolo.nome LIKE (?) 
                        OR articolo.descrizione LIKE (?)))
                ORDER BY asta.scadenza
                """)) {
            query.setString(1, "%"+search+"%");
            query.setString(2, "%"+search+"%");

            try (var res = query.executeQuery()) {
                Map<Integer, Auction> auctions = new HashMap<>();
                while (res.next()) {
                    int id = res.getInt(1);
                    var currAuction = auctions.get(id);
                    if (currAuction == null)
                        auctions.put(id, currAuction = new Auction(id,
                                res.getTimestamp(2).toLocalDateTime(),
                                new ArrayList<>(),
                                res.getDouble(3),
                                0.0));

                    currAuction.articles().add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            res.getString(7),
                            res.getDouble(8)));
                }

                return List.copyOf(auctions.values());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
