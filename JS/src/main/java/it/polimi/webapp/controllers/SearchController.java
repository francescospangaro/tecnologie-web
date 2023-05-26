package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class SearchController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var search = HttpServlets.getParameterOr(req, "search", "");
        if(search.isEmpty()) {
            resp.setContentType("application/json");
            gson.toJson(Collections.emptyList(), resp.getWriter());
            return;
        }

        try (var connection = dataSource.getConnection()) {
            var result = new AuctionDao(connection).findAuctionByWord(search);
            resp.setContentType("application/json");
            //print auction by ids
            gson.toJson(result, resp.getWriter());
        } catch (SQLException e) {
            LOGGER.error("Failed to findAuctionByWord({})", search, e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorSearchQuery"), resp.getWriter());
        }
    }

}
