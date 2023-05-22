package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.User;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDao.class);

    private final Connection connection;

    public LoginDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * Looks for the user with that id
     * if found the password is then checked locally
     * if not found returns an error
     */
    public @Nullable User findUser(String userName, String password) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT idUtente, nome, email, password
                FROM utente
                WHERE email = ?
                """)) {
            query.setString(1, userName);

            try (var res = query.executeQuery()) {
                if (res.next() && res.getString(4).equals(password))
                    return new User(res.getInt(1), res.getString(2), res.getString(3));
            } catch (SQLException e) {
                LOGGER.error("Failed to execute findUser query", e);
                return null;
            }
        }
        return null;
    }
}
