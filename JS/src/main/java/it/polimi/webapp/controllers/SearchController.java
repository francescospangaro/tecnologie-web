package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class SearchController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (var connection = dataSource.getConnection()) {
            var result = new AuctionDao(connection).findAuctionByWord(req.getParameter("search"));
            resp.setContentType("application/json");
            //print auction by ids
            gson.toJson(result, resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
