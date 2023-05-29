package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.InsertionState;
import it.polimi.webapp.beans.SellPageArgs;
import it.polimi.webapp.dao.AuctionDao;
import it.polimi.webapp.pages.Pages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

        if (articleIds.isEmpty() || minimumOfferDifference == null || minimumOfferDifference <= 0 || expiryDate == null || expiryDate.isBefore(LocalDateTime.now())) {
            Pages.forwardTo(Pages.SELL_PAGE, new SellPageArgs(
                    InsertionState.ERROR_DATA_FORMAT,
                    new SellPageArgs.AuctionData(minimumOfferDifference, expiryDate, articleIds)), req, resp);
            return;
        }

        boolean queryError;
        try (var connection = dataSource.getConnection()) {
            int result = new AuctionDao(connection).insertAuction(expiryDate, articleIds, minimumOfferDifference);
            queryError = result == 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to insert auction", e);
            queryError = true;
        }

        if (queryError) {
            Pages.forwardTo(Pages.SELL_PAGE, new SellPageArgs(
                    InsertionState.ERROR_QUERY,
                    new SellPageArgs.AuctionData(minimumOfferDifference, expiryDate, articleIds)), req, resp);
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }

}
