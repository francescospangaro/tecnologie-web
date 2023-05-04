package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;

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
        try (var connection = dataSource.getConnection();
             PreparedStatement selectUsers = connection.prepareStatement("SELECT idUtente, nome, email, password FROM utente");
             var results = selectUsers.executeQuery()
        ) {
            boolean found = false;
            while (results.next()) {
                if (results.getString(3).equals(username) && results.getString(4).equals(password)) {
                    //found user, and password is correct
                    found = true;
                    req.getSession(true).setAttribute("userId", results.getInt(1));
                    req.getSession().setAttribute("user", results.getString(2));
                    req.getSession().setAttribute("loginTime", LocalDateTime.now());
                    break;
                }
            }
            if (!found) {
                var dispatcher = Objects.requireNonNull(req.getRequestDispatcher("/login"), "Missing dispatcher");
                req.setAttribute("errorNotFound", true);
                dispatcher.forward(req, resp);
                return;
            }
            resp.sendRedirect(getServletContext().getContextPath() + "/home");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
