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
import java.util.Objects;

public class OpenAuctionController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAuctionController.class);
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
            LOGGER.error("Failed to findAuctions({}, closed: false)", session.id(), e);
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorOpenQuery"), resp.getWriter());
        }
    }
}
