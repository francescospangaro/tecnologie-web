package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.beans.Offer;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;
import it.polimi.webapp.dao.OffersDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class OffersController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var session = HttpServlets.requireSession(req);
        int auctionId = -1;

        boolean dataError = false;

        try {
            auctionId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            dataError = true;
        }

        double offerPrice = -1;
        try {
            offerPrice = Double.parseDouble(req.getParameter("offerValue"));
        } catch (NumberFormatException e) {
            dataError = true;
        }

        if (dataError) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
            return;
        }

        var offer = new Offer(session.id(), auctionId, offerPrice, session.loginTime());

        OffersDao.InsertionReturn inserted;

        try (var connection = dataSource.getConnection()) {
            inserted = new OffersDao(connection).insertOffer(offer);
            if (inserted.type() != OffersDao.TypeError.DONE) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError(inserted.type() == OffersDao.TypeError.LOWER_THAN_MAX ?
                        "errorMaxOffer"
                        : (inserted.type() == OffersDao.TypeError.LOWER_THAN_ARTICLE ?
                        "errorLowPrice" :
                        "errorQuery")), resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.setContentType("application/json");
        assert inserted.id() != null;
        gson.toJson(new InsertionSuccessful(inserted.id()), resp.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getParameter("id") != null) {
            int auctionId;
            try {
                auctionId = Integer.parseInt(req.getParameter("id"));
            } catch (NumberFormatException e) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
                return;
            }
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findOpenAuctionById(auctionId);
                resp.setContentType("application/json");
                //print auction by ids
                gson.toJson(Objects.requireNonNullElseGet(result,
                        () -> new ParsingError("errorQuery")), resp.getWriter());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {

        }
    }
}