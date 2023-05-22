package it.polimi.webapp.controllers.auction;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@MultipartConfig(
        maxFileSize = 1024 * 1024 * 10,      // 10 MB
        maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class AuctionController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        boolean wrongInsertedData = false;

        List<Integer> articleIds = null;
        try {
            var stringArticleIds = req.getParameterValues("selectedArticles");
            articleIds = stringArticleIds == null ? null : Arrays.stream(stringArticleIds)
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException ex) {
            wrongInsertedData = true;
        }

        int minimumOfferDifference = -1;
        try {
            minimumOfferDifference = Integer.parseInt(req.getParameter("minimumOfferDifference"));
        } catch (NumberFormatException ex) {
            wrongInsertedData = true;
        }

        LocalDateTime expiryDate = null;
        try {
            var dateStr = req.getParameter("expiryDate");
            expiryDate = dateStr != null ? LocalDateTime.parse(dateStr) : null;
        } catch (DateTimeParseException ex) {
            wrongInsertedData = true;
        }

        wrongInsertedData = wrongInsertedData
                            || articleIds == null || articleIds.isEmpty()
                            || minimumOfferDifference <= 0
                            || expiryDate == null || expiryDate.isBefore(LocalDateTime.now());

        if (wrongInsertedData) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorAuctionDataInserted"), resp.getWriter());
            return;
        }

        // Make NullAway happy, 'cause it can't infer that these are affectively non-null
        Objects.requireNonNull(expiryDate);
        Objects.requireNonNull(articleIds);

        var auction = new Auction(
                expiryDate,
                articleIds.stream().map(Article::new).toList(),
                minimumOfferDifference);

        Integer inserted;

        try (var connection = dataSource.getConnection()) {
            inserted = new AuctionDao(connection).insertAuction(auction);
            if (inserted == null) {
                //error in query execution
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorAuctionQuery"), resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.setContentType("application/json");
        gson.toJson(new InsertionSuccessful(inserted), resp.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("id") != null) {
            int auctionId;
            try {
                auctionId = Integer.parseInt(req.getParameter("id"));
            } catch (NumberFormatException ex) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
                return;
            }
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findAuctionByIds(
                        (Integer) req.getSession().getAttribute("userId"), auctionId);
                resp.setContentType("application/json");
                //print auction by ids
                gson.toJson(Objects.requireNonNullElseGet(result,
                        () -> new ParsingError("errorQuery")), resp.getWriter());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (var connection = dataSource.getConnection()) {
                var boughtAuctions = new AuctionDao(connection)
                        .findUserBoughtAuctions((Integer) req.getSession().getAttribute("userId"));
                resp.setContentType("application/json");
                //print auction by user id
                gson.toJson(Objects.requireNonNullElseGet(boughtAuctions,
                        () -> new ParsingError("errorQuery")), resp.getWriter());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
