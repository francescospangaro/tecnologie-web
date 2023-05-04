package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.dao.ArticleDao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ArticleController extends HttpServlet {

    private DataSource dataSource;

    @Override
    @Initializer
    public void init() throws ServletException {
        //connects to the database
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/AsteDB");
        } catch (NamingException e) {
            throw new ServletException("Failed to get Context", e);
        }

        if (this.dataSource == null)
            throw new ServletException("Data source not found!");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        String articleName = req.getParameter("articleName");
        String articleDesc = req.getParameter("articleDesc");
        String articleImage = req.getParameter("articleImage");

        boolean dataError = articleName == null || articleName.isEmpty()
                || articleDesc == null || articleDesc.isEmpty()
                || articleImage == null || articleImage.isEmpty();

        double articlePrice = -1;
        try {
            articlePrice = Double.parseDouble(req.getParameter("articlePrice"));
        } catch (NumberFormatException e){
            dataError = true;
        }

        if(dataError) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/articleInsertion"), "Missing dispatcher");
            req.setAttribute("errorDataInserted", true);
            disp.forward(req, resp);
            return;
        }

        var article = new Article(articleName, articleDesc, articleImage, articlePrice, userId);

        try (var connection = dataSource.getConnection()) {
            int inserted = new ArticleDao(connection).insertArticle(article);
            if (inserted == 0) {
                // error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/articleInsertion"), "Missing dispatcher");
                req.setAttribute("errorQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("goodInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }
}
