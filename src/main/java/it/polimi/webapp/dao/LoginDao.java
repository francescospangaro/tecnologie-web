package it.polimi.webapp.dao;

import it.polimi.webapp.beans.Article;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginDao {

    private final Connection connection;

    public LoginDao(Connection connection) {
        this.connection = connection;
    }

    public List<String> findUser(String userName, String password) throws SQLException {
        try (var query = connection.prepareStatement("""
                SELECT idUtente, nome, email, password 
                FROM utente
                WHERE email = ?
                """)) {
            query.setString(1, userName);

            try (var res = query.executeQuery()) {
                if(res.next() && res.getString(4).equals(password)){
                    List<String> userData = new ArrayList<>();
                    userData.add(((Integer) res.getInt(1)).toString());
                    userData.add(res.getString(2));
                    return userData;
                }
            }catch (SQLException e){
                List<String> ret = new ArrayList<>();
                ret.add("");
                return ret;
            }
        }
        List<String> ret = new ArrayList<>();
        ret.add("");
        return ret;
    }
}
