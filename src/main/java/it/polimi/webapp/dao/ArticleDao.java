package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleDao {

    private final Connection connection;

    public ArticleDao(Connection connection) {
        this.connection = connection;
    }

    public @Nullable List<Article> findAllArticles(int userId) throws SQLException {
        try (var query = connection.prepareStatement("""
            SELECT codArticolo, nome, descrizione, immagine, prezzo, utente_idUtente
            FROM articolo
            WHERE utente_idUtente = ?
            """)) {
            query.setInt(1, userId);

            try (var res = query.executeQuery()) {
                List<Article> result = new ArrayList<>();
                while (res.next())
                    result.add(new Article(
                            res.getInt(1),
                            res.getString(2),
                            res.getString(3),
                            res.getString(4),
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

    public int insertArticle(Article article) throws SQLException {
        try (PreparedStatement insertArticle = connection.prepareStatement("""
            INSERT INTO articolo (nome, descrizione, immagine, prezzo, utente_idUtente)
            VALUES (?, ?, ?, ?, ?)
            """)) {
            insertArticle.setString(1, article.name());
            insertArticle.setString(2, article.description());
            insertArticle.setString(3, article.immagine());
            insertArticle.setDouble(4, article.prezzo());
            insertArticle.setInt(5, article.idUtente());
            return insertArticle.executeUpdate();
        }
    }
}