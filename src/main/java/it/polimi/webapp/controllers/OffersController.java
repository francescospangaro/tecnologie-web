package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;
import it.polimi.webapp.beans.Offer;
import it.polimi.webapp.dao.OffersDao;

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
import java.util.Objects;

public class OffersController extends HttpServlet {

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
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        LocalDateTime dateTime = LocalDateTime.parse(req.getSession().getAttribute("loginTime").toString());
        int auctionId = -1;

        boolean dataError = false;

        try {
            auctionId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e){
            dataError = true;
        }

        double offerPrice = -1;
        try {
            offerPrice = Double.parseDouble(req.getParameter("offerValue"));
        } catch (NumberFormatException e){
            dataError = true;
        }

        if(dataError) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
            req.setAttribute("errorQuery", true);
            disp.forward(req, resp);
            return;
        }

        var offer = new Offer(userId, auctionId, offerPrice, dateTime);

        try (var connection = dataSource.getConnection()) {
            int inserted = new OffersDao(connection).insertOffer(offer);
            if (inserted == 0) {
                // error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
                req.setAttribute("errorQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("errorQuery", false);
        req.setAttribute("goodInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/offers?id=" + auctionId);
    }
}