package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.beans.Offer;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;
import it.polimi.webapp.dao.OffersDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class OffersController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OffersController.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);
        var offerPrice = HttpServlets.getParameterOr(req, "offerValue", (Integer) null);

        if (auctionId == null || offerPrice == null) {
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
            LOGGER.error("Failed to findAuctions({}, closed: false)", session.id(), e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
            return;
        }

        resp.setContentType("application/json");
        gson.toJson(new InsertionSuccessful(Objects.requireNonNull(inserted.id())), resp.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);
        if (auctionId == null) {
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
            LOGGER.error("Failed to findOpenAuctionById({})", auctionId, e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
        }
    }
}