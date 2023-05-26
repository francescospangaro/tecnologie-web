package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.UserSession;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.LoginDao;
import jakarta.security.enterprise.identitystore.PasswordHash;
import org.glassfish.soteria.identitystores.hash.Pbkdf2PasswordHashImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController extends BaseController {

    private PasswordHash passwordHash;

    @Override
    public void init() throws ServletException {
        super.init();
        // Kind of bad that we need to instantiate the impl ourselves, but we don't have CDI
        passwordHash = new Pbkdf2PasswordHashImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = HttpServlets.getParameterOr(req, "user", "");
        String password = HttpServlets.getParameterOr(req, "pass", "");

        if (username.isEmpty() || password.isEmpty()) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorCred"), resp.getWriter());
            return;
        }

        try (var connection = dataSource.getConnection()) {
            LoginDao loginDao = new LoginDao(connection, passwordHash);
            var user = loginDao.findUser(username, password.toCharArray());
            if (user == null) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorNotFound"), resp.getWriter());
                return;
            }

            var session = req.getSession(true);
            session.setAttribute("user", new UserSession(user.id(), user.name(), LocalDateTime.now()));

            resp.setContentType("application/json");
            gson.toJson(user, resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
