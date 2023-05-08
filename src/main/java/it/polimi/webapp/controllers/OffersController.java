package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.beans.Offer;
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
            var inserted = new OffersDao(connection).insertOffer(offer);
            if(inserted != OffersDao.InsertionResult.DONE) {
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
                req.setAttribute(
                        inserted == OffersDao.InsertionResult.LOWER_THAN_MAX ?
                                "errorMaxOffer"
                                : (inserted == OffersDao.InsertionResult.LOWER_THAN_ARTICLE ?
                                "errorLowPrice" :
                                "errorQuery"),
                        true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("errorMaxOffer", false);
        req.setAttribute("errorLowOffer", false);
        req.setAttribute("errorQuery", false);
        req.setAttribute("goodInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/offers?id=" + auctionId);
    }
}