package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.InsertionState;
import it.polimi.webapp.beans.SellPageArgs;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.pages.SellPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;

@MultipartConfig(
        maxFileSize = 1024 * 1024 * 10,      // 10 MB
        maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class ArticleController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        var session = HttpServlets.requireSession(req);
        String articleName = HttpServlets.getParameterOr(req, "articleName", "");
        String articleDesc = HttpServlets.getParameterOr(req, "articleDesc", "");
        Double articlePrice = HttpServlets.getParameterOr(req, "articlePrice", (Double) null);
        InputStream imageStream = HttpServlets.getImageOrNull(req, "articleImage");

        if (articleName.isEmpty() || articleDesc.isEmpty() || articlePrice == null || imageStream == null) {
            SellPage.forwardWith(req, resp, new SellPageArgs(
                    InsertionState.ERROR_DATA_FORMAT,
                    new SellPageArgs.ArticleData(articleName, articleDesc, articlePrice)));
            return;
        }

        var article = new Article(articleName, articleDesc, "", articlePrice, session.id());
        var queryError = false;
        try (var connection = dataSource.getConnection()) {
            int inserted = new ArticleDao(connection).insertArticle(article, Objects.requireNonNull(imageStream));

            queryError = inserted == 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to execute article insertion query", e);
            queryError = true;
        }

        if (queryError) {
            SellPage.forwardWith(req, resp, new SellPageArgs(
                    InsertionState.ERROR_QUERY,
                    new SellPageArgs.ArticleData(articleName, articleDesc, articlePrice)));
            return;
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }
}
