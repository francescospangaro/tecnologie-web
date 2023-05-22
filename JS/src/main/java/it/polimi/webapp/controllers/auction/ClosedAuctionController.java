package it.polimi.webapp.controllers.auction;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class ClosedAuctionController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClosedAuctionController.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);
        if(auctionId == null) {
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

            resp.setContentType("application/json");
            resp.getWriter().write("{}");
        } catch (SQLException e) {
            LOGGER.error("Failed to closeAuction({})", auctionId, e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorQuery"), resp.getWriter());
        }
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
            LOGGER.error("Failed to get findAuctions({}, closed: true)", session.id(), e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorClosedQuery"), resp.getWriter());
        }
    }

}