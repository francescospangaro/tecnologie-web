package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.UserSession;
import it.polimi.webapp.beans.LoginPageArgs;
import it.polimi.webapp.dao.LoginDao;
import it.polimi.webapp.pages.Pages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController extends BaseController {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = HttpServlets.getParameterOr(req, "user", "");
        String password = HttpServlets.getParameterOr(req, "pass", "");

        if (username.isEmpty() || password.isEmpty()) {
            Pages.forwardTo(Pages.LOGIN_PAGE, LoginPageArgs.parseError(username), req, resp);
            return;
        }

        try (var connection = dataSource.getConnection()) {
            var user = new LoginDao(connection).findUser(username, password);
            if (user == null) {
                Pages.forwardTo(Pages.LOGIN_PAGE, LoginPageArgs.notFound(username), req, resp);
                return;
            }

            var session = req.getSession(true);
            session.setAttribute("user", new UserSession(user.id(), user.name(), LocalDateTime.now()));

            resp.sendRedirect(getServletContext().getContextPath() + "/home");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
