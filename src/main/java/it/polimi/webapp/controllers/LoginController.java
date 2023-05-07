package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;
import it.polimi.webapp.dao.LoginDao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class LoginController extends HttpServlet {
    private DataSource dataSource;

    @Override
    @Initializer
    public void init() throws ServletException {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/AsteDB");
        } catch (NamingException e) {
            throw new ServletException("Failed to get Context", e);
        }

        if (this.dataSource == null)
            throw new ServletException("Data source not found!");
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("user");
        String password = req.getParameter("pass");
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/login"), "Missing dispatcher");
            req.setAttribute("errorCred", true);
            disp.forward(req, resp);
            return;
        }

        try (var connection = dataSource.getConnection()) {
            LoginDao loginDao = new LoginDao(connection);
            List<String> userData = loginDao.findUser(username, password);
            if (userData.get(0).equals("")) {
                var dispatcher = Objects.requireNonNull(req.getRequestDispatcher("/login"), "Missing dispatcher");
                req.setAttribute("errorNotFound", true);
                dispatcher.forward(req, resp);
                return;
            }
            //found user, and password is correct
            req.getSession(true).setAttribute("userId", Integer.parseInt(userData.get(0)));
            req.getSession().setAttribute("user", userData.get(1));
            req.getSession().setAttribute("loginTime", LocalDateTime.now());
            resp.sendRedirect(getServletContext().getContextPath() + "/home");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
