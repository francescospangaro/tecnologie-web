package it.polimi.webapp.controllers.auction;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class OpenAuctionController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);
        try (var connection = dataSource.getConnection()) {
            var closedAuction = new AuctionDao(connection).findAuctions(session.id(), false);
            resp.setContentType("application/json");
            //print open auctions
            gson.toJson(Objects.requireNonNullElseGet(closedAuction,
                    () -> new ParsingError("errorOpenQuery")), resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
