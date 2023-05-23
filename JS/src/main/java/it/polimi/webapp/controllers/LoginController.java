package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.UserSession;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.LoginDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController extends BaseController {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("user");
        String password = req.getParameter("pass");
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorCred"), resp.getWriter());
            return;
        }

        try (var connection = dataSource.getConnection()) {
            LoginDao loginDao = new LoginDao(connection);
            var user = loginDao.findUser(username, password);
            if (user == null) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorNotFound"), resp.getWriter());
                return;
            }

            var session = req.getSession(true);
            session.setAttribute("user", new UserSession(user.id(), user.name(), LocalDateTime.now()));

            resp.setContentType("application/json");
            gson.toJson(/* TODO: what object here? */ resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
