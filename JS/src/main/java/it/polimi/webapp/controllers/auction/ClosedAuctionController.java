package it.polimi.webapp.controllers.auction;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class ClosedAuctionController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);

        int auctionId;
        try {
            auctionId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("notFound"), resp.getWriter());
            return;
        }

        try(var connection = dataSource.getConnection()) {
            var res = new AuctionDao(connection).closeAuction(auctionId, session.loginTime(), session.id());
            if(res == 0) {
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("notFound"), resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.setContentType("application/json");
        resp.getWriter().write("{}");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);

        try (var connection = dataSource.getConnection()) {
            var closedAuction = new AuctionDao(connection).findAuctions(session.id(), true);
            resp.setContentType("application/json");
            //print closed auctions
            gson.toJson(Objects.requireNonNullElseGet(closedAuction,
                    () -> new ParsingError("errorClosedQuery")), resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}