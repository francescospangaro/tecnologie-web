package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CloseAuctionController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int auctionId;
        try {
            auctionId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            resp.sendError(404);
            return;
        }

        try(var connection = dataSource.getConnection()) {
            var res = new AuctionDao(connection).closeAuction(auctionId,
                    (LocalDateTime) req.getSession().getAttribute("loginTime"),
                    (Integer) req.getSession().getAttribute("userId"));
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