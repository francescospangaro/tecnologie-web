package it.polimi.webapp.dao;

import it.polimi.webapp.Transactions;
import it.polimi.webapp.beans.*;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionDao {
    private final Connection connection;
    private final Clock clock;

    public AuctionDao(Connection connection) {
        this(connection, Clock.systemDefaultZone());
    }

    public AuctionDao(Connection connection, Clock clock) {
        this.connection = connection;
        this.clock = clock;
    }

    public @Nullable List<Auction> findAuctions(int userId, boolean closed) throws SQLException {

        try (var query = connection.prepareStatement("""
                SELECT a.idAsta, a.scadenza, a.rialzoMin,
                       articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo,
                       offerta.prezzoOfferto
                FROM articolo
                       JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                       JOIN asta as a ON astearticoli.asta_idAsta = a.idAsta
                       LEFT OUTER JOIN offerta ON a.idAsta = offerta.asta_idAsta
                WHERE articolo.utente_idUtente=?
                      AND a.chiusa=?
                      AND (offerta.idOfferta IS NULL OR offerta.prezzoOfferto IN (
                          select MAX(o1.prezzoOfferto)
                          from offerta as o1
                          WHERE o1.asta_idAsta = a.idAsta
                      ))
                ORDER BY a.scadenza""")) {
            query.setInt(1, userId);
            query.setInt(2, closed ? 1 : 0);

            try (var res = query.executeQuery()) {
                Map<Integer, Auction> auctions = new LinkedHashMap<>(); // Keep the order given by the query
                while (res.next()) {
                    int id = res.getInt(1);
                    var currAuction = auctions.get(id);
                    if (currAuction == null)
                        auctions.put(id, currAuction = new Auction(id,
                                res.getTimestamp(2).toLocalDateTime(),
                                new ArrayList<>(),
                                res.getInt(3),
                                res.getDouble(9)));

                    currAuction.articles().add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            Base64.getEncoder().encodeToString(res.getBytes(7)),
                            res.getDouble(8),
                            userId));
                }

                return List.copyOf(auctions.values());
            }
        }
    }

    /**
     * Method returns the auction opened by the logged user,
     * then returns an auction object created by checking
     * if said auction is either open or closed
     */
    public @Nullable ExtendedAuction findAuctionByIds(int userId, int auctionId) throws SQLException {
        Auction baseAuction = null;
        boolean closed = false;

        try (var query = connection.prepareStatement("""
                SELECT asta.scadenza, asta.rialzoMin, asta.chiusa,
                       articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo
                FROM articolo
                     JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                     JOIN asta ON astearticoli.asta_idAsta = asta.idAsta
                WHERE articolo.utente_idUtente=? AND asta_idAsta = ?""")) {
            query.setInt(1, userId);
            query.setInt(2, auctionId);

            try (var res = query.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (res.next()) {
                    if (baseAuction == null) {
                        baseAuction = new Auction(
                                auctionId,
                                res.getTimestamp(1).toLocalDateTime(),
                                articles,
                                res.getInt(2));
                        closed = res.getBoolean(3);
                    }

                    articles.add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            Base64.getEncoder().encodeToString(res.getBytes(7)),
                            res.getDouble(8),
                            userId));
                }

                if (baseAuction == null)
                    return null;

                baseAuction = baseAuction.withArticles(List.copyOf(articles));
            }
        }

        return closed
                ? doPopulateClosedAuction(baseAuction)
                : doPopulateOpenAuction(baseAuction);
    }

    public List<ClosedAuction> findUserBoughtAuctions(int userId) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT asta.idAsta, asta.scadenza, asta.rialzoMin, articolo.codArticolo,
                       articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo, offerta.prezzoOfferto, utente.nome, utente.indirizzo
                FROM articolo
                         JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                         JOIN asta ON asta.idAsta = astearticoli.asta_idAsta
                         JOIN offerta ON offerta.asta_idAsta = asta.idAsta
                         JOIN utente ON utente.idUtente = offerta.utente_idUtente
                WHERE offerta.utente_idUtente = ?
                  AND asta.chiusa = 1
                  AND offerta.prezzoOfferto IN (
                    SELECT MAX(o.prezzoOfferto)
                    FROM offerta as o
                    WHERE o.asta_idAsta = asta.idAsta
                )
                """)) {
            query.setInt(1, userId);
            try (var res = query.executeQuery()) {
                Map<Integer, ClosedAuction> auctions = new LinkedHashMap<>(); // Keep the order given by the query
                while (res.next()) {
                    int id = res.getInt(1);
                    var closedAuction = auctions.get(id);
                    if(closedAuction == null) {
                        auctions.put(id, closedAuction = new ClosedAuction(
                                new Auction(
                                        res.getInt(1),
                                        res.getTimestamp(2).toLocalDateTime(),
                                        new ArrayList<>(),
                                        res.getInt(3)),
                                res.getDouble(9),
                                res.getString(10),
                                res.getString(11)));
                    }

                    closedAuction.base().articles().add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            Base64.getEncoder().encodeToString(res.getBytes(7)),
                            res.getDouble(8),
                            userId));
                }

                return List.copyOf(auctions.values());
            }
        }
    }

    private @Nullable ClosedAuction doPopulateClosedAuction(Auction base) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT offerta.prezzoOfferto, utente.nome, utente.indirizzo
                FROM utente
                    JOIN offerta ON offerta.utente_idUtente = utente.idUtente
                    JOIN asta AS a ON offerta.asta_idAsta = a.idAsta
                WHERE offerta.asta_idAsta = ?
                  AND a.chiusa = true
                  AND offerta.prezzoOfferto IN (
                    select MAX(offerta.prezzoOfferto)
                    from offerta
                    WHERE offerta.asta_idAsta = a.idAsta
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
                FROM utente
                    JOIN offerta ON utente.idUtente = offerta.utente_idUtente
                    JOIN asta ON offerta.asta_idAsta = asta.idAsta
                WHERE offerta.asta_idAsta = ? AND asta.chiusa = false
                ORDER BY offerta.dataOfferta DESC
                """)) {
            query.setInt(1, base.id());
            try (var res = query.executeQuery()) {
                List<Offer> offers = new ArrayList<>();
                while (res.next())
                    offers.add(new Offer(
                            res.getInt(1),
                            res.getInt(2),
                            res.getInt(3),
                            res.getDouble(4),
                            res.getString(5),
                            res.getTimestamp(6).toLocalDateTime()));
                return new OpenAuction(base, offers);
            }
        }
    }

    /**
     * Returns ONLY the open auctions that have the specified ID
     */
    public @Nullable OpenAuction findOpenAuctionById(int auctionId) throws SQLException {
        Auction baseAuction = null;
        try (var query = connection.prepareStatement("""
                SELECT a.idAsta, a.scadenza, a.rialzoMin,
                       articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo,
                       offerta.prezzoOfferto,
                       (
                          SELECT SUM(articolo.prezzo)
                          FROM articolo
                               JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                               JOIN asta ON astearticoli.asta_idAsta = asta.idAsta
                          WHERE asta.idAsta = a.idAsta
                       )
                FROM articolo
                     JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                     JOIN asta AS a ON astearticoli.asta_idAsta = a.idAsta
                     LEFT OUTER JOIN offerta ON a.idAsta = offerta.asta_idAsta
                WHERE a.chiusa=false
                  AND a.scadenza > ?
                  AND a.idAsta = ?
                  AND (offerta.idOfferta IS NULL OR offerta.prezzoOfferto IN (
                    select MAX(o.prezzoOfferto)
                    from offerta as o
                    WHERE o.asta_idAsta = a.idAsta
                  ))
                ORDER BY a.scadenza""")) {
            query.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(clock)));
            query.setInt(2, auctionId);

            try (var res = query.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (res.next()) {
                    if (baseAuction == null) {
                        baseAuction = new Auction(
                                auctionId,
                                res.getTimestamp(2).toLocalDateTime(),
                                articles,
                                res.getInt(3),
                                res.getObject(9) == null
                                        ? res.getDouble(9)
                                        : res.getDouble(10));
                    }

                    articles.add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            Base64.getEncoder().encodeToString(res.getBytes(7)),
                            res.getDouble(8),
                            -1));
                }

                if (baseAuction == null)
                    return null;

                baseAuction = baseAuction.withArticles(List.copyOf(articles));
            }
        }

        return doPopulateOpenAuction(baseAuction);
    }

    /**
     * Inserts an auction in the DB, also connects the auction to its articles
     * with the table astearticoli
     */
    public @Nullable Integer insertAuction(LocalDateTime expiry,
                                           List<Integer> articleIds,
                                           int minimumOfferDifference) throws SQLException {
        return Transactions.startNullable(connection, Transactions.Type.NESTED, tx -> {
            int generatedId;
            try (PreparedStatement insertAuction = tx.prepareStatement(
                    "INSERT INTO asta (rialzoMin, scadenza) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertAuction.setInt(1, minimumOfferDifference);
                insertAuction.setTimestamp(2, Timestamp.valueOf(expiry));
                int res = insertAuction.executeUpdate();
                if (res == 0)
                    return null;

                try (var generatedKeys = insertAuction.getGeneratedKeys()) {
                    if (!generatedKeys.next())
                        throw new SQLException("Creating auction failed, no ID obtained.");

                    generatedId = generatedKeys.getInt(1);
                }
            }

            try (var query = connection.prepareStatement(String.format("""
                    SELECT articolo.codArticolo
                    FROM articolo
                          JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                          JOIN asta ON astearticoli.asta_idAsta = asta.idAsta
                    WHERE articolo.codArticolo IN %s AND asta.chiusa = true
                    """,
                    articleIds.stream()
                            .map(ignored -> "?")
                            .collect(Collectors.joining(", ", "(", ")")))
            )) {
                for(int idx = 0; idx < articleIds.size(); idx++)
                    query.setInt(idx + 1, articleIds.get(idx));

                try (var res = query.executeQuery()) {
                    if (res.next()) {
                        connection.rollback();
                        return 0;
                    }
                }
            }

            try (PreparedStatement relate = tx.prepareStatement(
                    "INSERT INTO astearticoli (articolo_codArticolo, asta_idAsta) VALUES (?, ?)")
            ) {
                for (int articleId : articleIds) {
                    relate.setInt(1, articleId);
                    relate.setInt(2, generatedId);
                    relate.addBatch();
                }

                relate.executeBatch();
            }

            return generatedId;
        });
    }

    public int closeAuction(int auctionId, int userId) throws SQLException {
        try (PreparedStatement close = connection.prepareStatement("""
                UPDATE asta INNER JOIN (
                    SELECT a.idAsta as oldId
                    FROM asta as a
                         JOIN astearticoli ON a.idAsta = astearticoli.asta_idAsta
                         JOIN articolo ON astearticoli.articolo_codArticolo = articolo.codArticolo
                    WHERE articolo.utente_idUtente = ?
                ) as joinedAuctions
                ON joinedAuctions.oldId = asta.idAsta
                SET asta.chiusa = true
                WHERE asta.idAsta = ? and asta.scadenza < ?
                """)) {
            close.setInt(1, userId);
            close.setInt(2, auctionId);
            close.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now(clock)));
            return close.executeUpdate();
        }
    }

    public List<Auction> findAuctionByWord(String search) throws SQLException {
        Timestamp requestTime = Timestamp.valueOf(LocalDateTime.now(clock));
        try (var query = connection.prepareStatement("""
                SELECT a.idAsta, a.scadenza, a.rialzoMin,
                       articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo,
                       offerta.prezzoOfferto,
                       (
                           SELECT SUM(articolo.prezzo)
                           FROM articolo
                               JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                               JOIN asta ON astearticoli.asta_idAsta = asta.idAsta
                           WHERE asta.idAsta = a.idAsta
                       )
                FROM articolo
                     JOIN astearticoli ON articolo.codArticolo = astearticoli.articolo_codArticolo
                     JOIN asta AS a ON astearticoli.asta_idAsta = a.idAsta
                     LEFT OUTER JOIN offerta ON a.idAsta = offerta.asta_idAsta
                WHERE a.chiusa=false
                  AND a.scadenza > ?
                  AND (offerta.idOfferta IS NULL OR offerta.prezzoOfferto IN (
                    select MAX(o.prezzoOfferto)
                    from offerta AS o
                    WHERE o.asta_idAsta = a.idAsta
                  ))
                  AND a.idAsta IN (
                    SELECT astearticoli.asta_idAsta
                    from astearticoli JOIN articolo ON astearticoli.articolo_codArticolo = articolo.codArticolo
                    WHERE articolo.nome LIKE (?) OR articolo.descrizione LIKE (?))
                ORDER BY a.scadenza
                """)) {
            query.setTimestamp(1, requestTime);
            query.setString(2, "%" + search + "%");
            query.setString(3, "%" + search + "%");

            try (var res = query.executeQuery()) {
                Map<Integer, Auction> auctions = new LinkedHashMap<>(); // keep the db order
                while (res.next()) {
                    int id = res.getInt(1);
                    var currAuction = auctions.get(id);
                    // if there are no offers gets the sum of the auction's articles' prices
                    if (currAuction == null)
                        auctions.put(id, currAuction = new Auction(id,
                                res.getTimestamp(2).toLocalDateTime(),
                                new ArrayList<>(),
                                res.getInt(3),
                                res.getObject(9) == null
                                        ? res.getDouble(9)
                                        : res.getDouble(10)));

                    currAuction.articles().add(new Article(
                            res.getInt(4),
                            res.getString(5),
                            res.getString(6),
                            Base64.getEncoder().encodeToString(res.getBytes(7)),
                            res.getDouble(8)));
                }
                return List.copyOf(auctions.values());
            }
        }
    }
}
