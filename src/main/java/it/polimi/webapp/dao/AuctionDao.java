package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.beans.ClosedAuction;
import it.polimi.webapp.beans.ExtendedAuction;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionDao {
    private final Connection connection;

    public AuctionDao(Connection connection) {
        this.connection = connection;
    }


    public @Nullable List<Auction> findAuctions(int userId, boolean closed) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT asta.idAsta, articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo, asta.rialzoMin, asta.scadenza
                FROM articolo, asta, astearticoli, utente
                WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                AND asta.idAsta=astearticoli.asta_idAsta
                AND articolo.utente_idUtente=?
                AND asta.chiusa=?""")) {
            query.setInt(1, userId);
            query.setInt(2, closed ? 1 : 0);
            try (var res = query.executeQuery()) {
                List<Auction> result = new ArrayList<>();
                while (res.next()) {
                    result.add(new Auction(
                            res.getInt(1),
                            res.getDate(8).toLocalDate(),
                            res.getDouble(7)));
                }

                if (result.size() > 0) {
                    return result;
                } else {
                    return null;
                }
            }
        }
    }

    public @Nullable ExtendedAuction findAuctionById(int userId, int auctionId) throws SQLException {
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
                    if(baseAuction == null) {
                        baseAuction = new Auction(auctionId, res.getDate(1).toLocalDate(), res.getDouble(2));
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

                if(baseAuction == null)
                    return null;

                baseAuction = baseAuction.withArticles(articles);
            }
        }

        return closed
                ? doPopulateClosedAuction(baseAuction)
                : doPopulateOpenAuction(baseAuction);
    }

    private @Nullable ClosedAuction doPopulateClosedAuction(Auction base) throws SQLException {
        return null; // TODO:
    }

    private @Nullable ClosedAuction doPopulateOpenAuction(Auction base) throws SQLException {
        return null; // TODO:
    }

    public int insertAuction(Auction auction) throws SQLException {
        try (PreparedStatement insertAuction = connection.prepareStatement(
                "INSERT INTO asta (rialzoMin, scadenza) VALUES (?, ?)")) {
            insertAuction.setDouble(1, auction.minimumOfferDifference());
            insertAuction.setDate(2, Date.valueOf(auction.expiry()));
            int res = insertAuction.executeUpdate();
            if (res == 0)
                return 0;

            try (var generatedKeys = insertAuction.getGeneratedKeys()) {
                if (!generatedKeys.next())
                    throw new SQLException("Creating auction failed, no ID obtained.");

                auction = auction.withId(generatedKeys.getInt(1));
            }

            try (PreparedStatement relate = connection.prepareStatement(
                    "INSERT INTO astearticoli (articolo_codArticolo, asta_idAsta) VALUES (?, ?)")
            ) {

                for (Integer articleId : auction.articles().stream()
                        .map(Article::codArticle)
                        .toList()) {
                    relate.setInt(1, articleId);
                    relate.setInt(2, auction.id());
                    relate.addBatch();
                }

                var result = relate.executeUpdate();
                if (result == 0)
                    return 0;
            }
        }
        return 1;
    }
}
