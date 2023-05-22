package it.polimi.webapp.controllers.auction;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class AuctionController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionController.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<Integer> articleIds = List.of();
        try {
            var stringArticleIds = req.getParameterValues("selectedArticles");
            articleIds = stringArticleIds == null ? articleIds : Arrays.stream(stringArticleIds)
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException ignored) {
            // parsing failed
        }

        Integer minimumOfferDifference = HttpServlets.getParameterOr(req, "minimumOfferDifference", (Integer) null);
        LocalDateTime expiryDate = HttpServlets.getParameterOr(req, "expiryDate", (LocalDateTime) null);

        if (articleIds.isEmpty() || minimumOfferDifference == null || expiryDate == null || expiryDate.isBefore(LocalDateTime.now())) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorAuctionDataInserted"), resp.getWriter());
            return;
        }

        var auction = new Auction(
                expiryDate,
                articleIds.stream().map(Article::new).toList(),
                minimumOfferDifference);

        boolean queryError;
        Integer inserted = -1;
        try (var connection = dataSource.getConnection()) {
            inserted = new AuctionDao(connection).insertAuction(auction);
            queryError = inserted == null;
        } catch (SQLException e) {
            LOGGER.error("Failed to insert auction", e);
            queryError = true;
        }

        if (queryError) {
            //error in query execution
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorAuctionQuery"), resp.getWriter());
            return;
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
