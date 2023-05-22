package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.UserSession;
import it.polimi.webapp.dao.LoginDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class LoginController extends BaseController {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("user");
        String password = req.getParameter("pass");
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/login"), "Missing dispatcher");
            req.setAttribute("errorCred", true);
            assert username != null;
            if(!username.isEmpty())
                req.setAttribute("loginUsername", username);
            disp.forward(req, resp);
            return;
        }

        try (var connection = dataSource.getConnection()) {
            LoginDao loginDao = new LoginDao(connection);
            var user = loginDao.findUser(username, password);
            if (user == null) {
                var dispatcher = Objects.requireNonNull(req.getRequestDispatcher("/login"), "Missing dispatcher");
                req.setAttribute("errorNotFound", true);
                req.setAttribute("loginUsername", username);
                dispatcher.forward(req, resp);
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
