package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class ArticleDao {

    private final Connection connection;

    public ArticleDao(Connection connection) {
        this.connection = connection;
    }

    public @Nullable List<Article> findAllArticles(int userId) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT a.codArticolo, a.nome, a.descrizione, a.immagine, a.prezzo, a.utente_idUtente
                FROM articolo as a
                WHERE utente_idUtente = ?
                AND a.codArticolo NOT IN (
                    SELECT a1.codArticolo
                    FROM articolo as a1, asta, astearticoli
                    WHERE a1.codArticolo = astearticoli.articolo_codArticolo
                    AND astearticoli.asta_idAsta = asta.idAsta
                    AND asta.chiusa = true
                )
                """)) {
            query.setInt(1, userId);

            try (var res = query.executeQuery()) {
                List<Article> result = new ArrayList<>();
                while (res.next())
                    result.add(new Article(
                            res.getInt(1),
                            res.getString(2),
                            res.getString(3),
                            Base64.getEncoder().encodeToString(res.getBytes(4)),
                            res.getDouble(5),
                            res.getInt(6)));

                if (result.size() > 0) {
                    return result;
                } else {
                    return null;
                }
            }
        }
    }

    public @Nullable Integer insertArticle(Article article, InputStream imageStream) throws SQLException {
        try (PreparedStatement insertArticle = connection.prepareStatement("""
                INSERT INTO articolo (nome, descrizione, immagine, prezzo, utente_idUtente)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            insertArticle.setString(1, article.name());
            insertArticle.setString(2, article.description());
            insertArticle.setBlob(3, imageStream);
            insertArticle.setDouble(4, article.prezzo());
            insertArticle.setInt(5, Objects.requireNonNull(article.idUtente(), "Missing user id"));

            if (insertArticle.executeUpdate() != 1)
                return null;

            try (var generatedKeys = insertArticle.getGeneratedKeys()) {
                if (!generatedKeys.next())
                    throw new SQLException("Creating auction failed, no ID obtained.");
                return generatedKeys.getInt(1);
            }
        }
    }
}