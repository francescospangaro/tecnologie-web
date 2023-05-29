package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class CloseAuctionController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);
        var auctionId = HttpServlets.getParameterOr(req, "id", (Integer) null);

        if (auctionId == null) {
            resp.sendError(404);
            return;
        }

        try(var connection = dataSource.getConnection()) {
            var res = new AuctionDao(connection).closeAuction(auctionId, session.id());
            if(res == 0) {
                resp.sendError(404);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }

}