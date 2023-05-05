package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.dao.AuctionDao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CloseAuctionController extends HttpServlet {

    private DataSource dataSource;

    @Override
    @Initializer
    public void init() throws ServletException {
        //connects to the database
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/AsteDB");
        } catch (NamingException e) {
            throw new ServletException("Failed to get Context", e);
        }

        if (this.dataSource == null)
            throw new ServletException("Data source not found!");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int auctionId;
        try {
            auctionId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            resp.sendError(404);
            return;
        }

        try(var connection = dataSource.getConnection()) {
            var res = new AuctionDao(connection).closeAuction(auctionId, (LocalDateTime) req.getSession().getAttribute("loginTime"), (Integer) req.getSession().getAttribute("userId"));
            if(res == 0) {
                resp.sendError(404);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }

}