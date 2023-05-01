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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AuctionController extends HttpServlet {
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
        Integer asta_idAsta = -1;
        String[] stringArticleIds = req.getParameterValues("selectedArticles");
        List<Integer> articleIds = new ArrayList<>(Arrays.stream(stringArticleIds)
                .map(Integer::parseInt)
                .toList());
        double minimumOfferDifference = Double.parseDouble(req.getParameter("minimumOfferDifference"));
        LocalDate expiryDate = LocalDate.parse(req.getParameter("expiryDate"));
        if (articleIds.get(0) == -1 || minimumOfferDifference == -1 || expiryDate.isBefore(LocalDate.now()) || minimumOfferDifference <= 0) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/auctionInsertion"), "Missing dispatcher");
            req.setAttribute("errorDataInserted", true);
            disp.forward(req, resp);
            return;
        }
        try (var connection = dataSource.getConnection();
             PreparedStatement insertAuction = connection.prepareStatement(
                     "INSERT INTO asta (rialzoMin, scadenza) VALUES (?, ?)")) {
            insertAuction.setDouble(1, minimumOfferDifference);
            insertAuction.setDate(2, Date.valueOf(expiryDate));
            var result = insertAuction.executeUpdate();
            if (result == 0) {
                //error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/auctionInsertion"), "Missing dispatcher");
                req.setAttribute("errorQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (var connection = dataSource.getConnection();
             PreparedStatement getAuction = connection.prepareStatement("SELECT idAsta FROM asta");
             var results = getAuction.executeQuery()
        ) {
            while (results.next()) {
                if (asta_idAsta < results.getInt(1)) {
                    //get last auction (the biggest id is the last added auction)
                    asta_idAsta = results.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (Integer articleId : articleIds) {
            try (var connection = dataSource.getConnection();
                 PreparedStatement relate = connection.prepareStatement(
                         "INSERT INTO astearticoli (articolo_codArticolo, asta_idAsta) VALUES (?, ?)")
            ) {
                relate.setInt(1, articleId);
                relate.setInt(2, asta_idAsta);
                var result = relate.executeUpdate();
                if (result == 0) {
                    //error in query execution
                    var disp = Objects.requireNonNull(req.getRequestDispatcher("/auctionInsertion"), "Missing dispatcher");
                    req.setAttribute("errorQuery", true);
                    disp.forward(req, resp);
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        req.setAttribute("goodAuctionInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }


}
