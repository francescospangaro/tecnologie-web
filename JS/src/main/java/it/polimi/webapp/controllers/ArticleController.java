package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.ParsingError;
import it.polimi.webapp.beans.InsertionSuccessful;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.dao.AuctionDao;

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

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        String articleName = req.getParameter("articleName");
        String articleDesc = req.getParameter("articleDesc");
        Part articleImage = req.getPart("articleImage");

        InputStream imageStream = null;
        String mimeType = null;

        String imageName = null;

        if(articleImage != null){
            imageStream = articleImage.getInputStream();
            imageName = articleImage.getSubmittedFileName();
            mimeType = getServletContext().getMimeType(imageName);
        }

        boolean dataError = articleName == null || articleName.isEmpty()
                            || articleDesc == null || articleDesc.isEmpty()
                            || Objects.requireNonNull(articleImage).getSize() == 0 || imageStream == null
                            || (imageStream.available()==0) || !Objects.requireNonNull(mimeType).startsWith("image/");

        double articlePrice = -1;
        try {
            articlePrice = Double.parseDouble(req.getParameter("articlePrice"));
        } catch (NumberFormatException e){
            dataError = true;
        }

        if(dataError) {
            resp.setContentType("application/json");
            gson.toJson(new ParsingError("errorArticleDataInserted"), resp.getWriter());
            return;
        }

        var article = new Article(articleName, articleDesc, Objects.requireNonNull(imageName), articlePrice, userId);

        Integer inserted;

        try (var connection = dataSource.getConnection()) {
            inserted = new ArticleDao(connection).insertArticle(article, Objects.requireNonNull(imageStream));
            if (inserted == null) {
                // error in query execution
                resp.setContentType("application/json");
                gson.toJson(new ParsingError("errorArticleQuery"), resp.getWriter());
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        resp.setContentType("application/json");
        gson.toJson(new InsertionSuccessful(inserted), resp.getWriter());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (var connection = dataSource.getConnection()) {
            var result = new ArticleDao(connection).findAllArticles(
                    (Integer) req.getSession().getAttribute("userId"));
            resp.setContentType("application/json");
            //print articles
            gson.toJson(Objects.requireNonNullElseGet(result,
                    () -> new ParsingError("errorArticlesQuery")), resp.getWriter());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
