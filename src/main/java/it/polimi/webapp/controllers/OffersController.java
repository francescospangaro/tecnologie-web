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
            int inserted = new OffersDao(connection).insertOffer(offer);
            switch(inserted){
                case -2 -> {
                    var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
                    System.out.println("Magia1");
                    req.setAttribute("errorMaxOffer", true);
                    disp.forward(req, resp);
                    return;
                }
                case -1 -> {
                    var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
                    System.out.println("Magia2");
                    req.setAttribute("errorLowOffer", true);
                    disp.forward(req, resp);
                    return;
                }
                case 0 -> {
                    var disp = Objects.requireNonNull(req.getRequestDispatcher("/offers"), "Missing dispatcher");
                    System.out.println("Magia3");
                    req.setAttribute("errorQuery", true);
                    disp.forward(req, resp);
                    return;
                }
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