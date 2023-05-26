package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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

        if (articleName.isEmpty() || articleDesc.isEmpty() || articlePrice == null || articlePrice <= 0 || imageStream == null) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorArticleDataInserted"), resp.getWriter());
            return;
        }

        var article = new Article(articleName, articleDesc, "", articlePrice, session.id());
        var queryError = false;

        Integer inserted = -1;
        try (var connection = dataSource.getConnection()) {
            inserted = new ArticleDao(connection).insertArticle(article, Objects.requireNonNull(imageStream));
            queryError = inserted == null;
        } catch (SQLException e) {
            LOGGER.error("Failed to execute article insertion query", e);
            queryError = true;
        }

        if (queryError) {
            // error in query execution
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorArticleQuery"), resp.getWriter());
            return;
        }

        resp.setContentType("application/json");
        gson.toJson(new InsertionSuccessful(Objects.requireNonNull(inserted)), resp.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = HttpServlets.requireSession(req);
        try (var connection = dataSource.getConnection()) {
            var result = new ArticleDao(connection).findAllArticles(session.id());
            resp.setContentType("application/json");
            //print articles
            gson.toJson(Objects.requireNonNullElseGet(result,
                    () -> new ParsingError("errorArticlesQuery")), resp.getWriter());
        } catch (SQLException e) {
            LOGGER.error("Failed to findAllArticles({})", session.id(), e);

            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorArticlesQuery"), resp.getWriter());
        }
    }
}
