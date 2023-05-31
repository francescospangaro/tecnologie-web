package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.OffersPageArgs;
import it.polimi.webapp.dao.OffersDao;
import it.polimi.webapp.pages.Pages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OffersController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OffersController.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        var session = HttpServlets.requireSession(req);
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);
        var offerPrice = HttpServlets.getParameterOr(req, "offerValue", (Double) null);

        if (auctionId == null || offerPrice == null || offerPrice <= 0) {
            Pages.forwardTo(Pages.OFFERS_PAGE, new OffersPageArgs(auctionId, offerPrice,
                    OffersDao.InsertionResult.DB_ERROR), req, resp);
            return;
        }

        try (var connection = dataSource.getConnection()) {
            var inserted = new OffersDao(connection).insertOffer(session.id(), auctionId, offerPrice, LocalDateTime.now());
            if (inserted != OffersDao.InsertionResult.DONE) {
                Pages.forwardTo(Pages.OFFERS_PAGE, new OffersPageArgs(auctionId, offerPrice, inserted), req, resp);
                return;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to insert offer", e);

            Pages.forwardTo(Pages.OFFERS_PAGE, new OffersPageArgs(auctionId, offerPrice,
                    OffersDao.InsertionResult.DB_ERROR), req, resp);
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/offers?id=" + auctionId);
    }
}