package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.UserSession;
import it.polimi.webapp.beans.LoginPageArgs;
import it.polimi.webapp.dao.LoginDao;
import it.polimi.webapp.pages.Pages;
import jakarta.security.enterprise.identitystore.PasswordHash;
import org.glassfish.soteria.identitystores.hash.Pbkdf2PasswordHashImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private PasswordHash passwordHash;

    @Override
    public void init() throws ServletException {
        super.init();
        // Kind of bad that we need to instantiate the impl ourselves, but we don't have CDI
        passwordHash = new Pbkdf2PasswordHashImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = HttpServlets.getParameterOr(req, "user", "");
        String password = HttpServlets.getParameterOr(req, "pass", "");

        if (username.isEmpty() || password.isEmpty()) {
            Pages.forwardTo(Pages.LOGIN_PAGE, LoginPageArgs.parseError(username), req, resp);
            return;
        }

        try (var connection = dataSource.getConnection()) {
            var user = new LoginDao(connection, passwordHash).findUser(username, password.toCharArray());
            if (user == null) {
                Pages.forwardTo(Pages.LOGIN_PAGE, LoginPageArgs.notFound(username), req, resp);
                return;
            }

            var session = req.getSession(true);
            session.setAttribute("user", new UserSession(user.id(), user.name(), LocalDateTime.now()));

            resp.sendRedirect(getServletContext().getContextPath() + "/home");
        } catch (SQLException e) {
            LOGGER.error("Failed to login", e);

            Pages.forwardTo(Pages.LOGIN_PAGE, LoginPageArgs.queryError(username), req, resp);
        }
    }
}
