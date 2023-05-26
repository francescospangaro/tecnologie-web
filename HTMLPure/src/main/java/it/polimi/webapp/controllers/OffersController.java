package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.Offer;
import it.polimi.webapp.beans.OffersPageArgs;
import it.polimi.webapp.dao.OffersDao;
import it.polimi.webapp.pages.Pages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class OffersController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        var session = HttpServlets.requireSession(req);
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);
        var offerPrice = HttpServlets.getParameterOr(req, "offerValue", (Integer) null);

        if (auctionId == null || offerPrice == null || offerPrice <= 0) {
            Pages.forwardTo(Pages.OFFERS_PAGE, new OffersPageArgs(auctionId, offerPrice,
                    OffersDao.InsertionResult.DB_ERROR), req, resp);
            return;
        }

        var offer = new Offer(session.id(), auctionId, offerPrice, session.loginTime());

        try (var connection = dataSource.getConnection()) {
            var inserted = new OffersDao(connection).insertOffer(offer);
            if (inserted != OffersDao.InsertionResult.DONE) {
                Pages.forwardTo(Pages.OFFERS_PAGE, new OffersPageArgs(auctionId, offerPrice, inserted), req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/offers?id=" + auctionId);
    }
}