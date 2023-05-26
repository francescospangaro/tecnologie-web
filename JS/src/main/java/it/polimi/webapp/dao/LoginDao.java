package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.User;
import jakarta.security.enterprise.identitystore.PasswordHash;
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
    private final PasswordHash passwordHash;

    public LoginDao(Connection connection, PasswordHash passwordHash) {
        this.connection = connection;
        this.passwordHash = passwordHash;
    }

    /**
     * Looks for the user with that id
     * if found the password is then checked locally
     * if not found returns an error
     */
    public @Nullable User findUser(String userName, char[] password) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT idUtente, nome, email, password
                FROM utente
                WHERE email = ?
                """)) {
            query.setString(1, userName);

            try (var res = query.executeQuery()) {
                if (res.next() && passwordHash.verify(password, res.getString(4)))
                    return new User(res.getInt(1), res.getString(2), res.getString(3));
            } catch (SQLException e) {
                LOGGER.error("Failed to execute findUser query", e);
                return null;
            }
        }
        return null;
    }
}
