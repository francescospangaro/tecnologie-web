package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
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
        double articlePrice = -1;
        if(articleName == null || articleName.isEmpty() || articleDesc == null || articleDesc.isEmpty() || articleImage == null || articleImage.isEmpty()){
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/articleInsertion"), "Missing dispatcher");
            req.setAttribute("errorDataInserted", true);
            disp.forward(req, resp);
            return;
        }
        try{
            articlePrice = Double.parseDouble(req.getParameter("articlePrice"));
        }catch (NumberFormatException e){
            //error in data inserted, so forwarding to the selling page with an error message
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/articleInsertion"), "Missing dispatcher");
            req.setAttribute("errorDataInserted", true);
            disp.forward(req, resp);
            return;
        }
        try (var connection = dataSource.getConnection();
             PreparedStatement insertArticle = connection.prepareStatement(
                     "INSERT INTO articolo (nome, descrizione, immagine, prezzo, utente_idUtente) VALUES (?, ?, ?, ?, ?)")
        ) {
            insertArticle.setString(1, articleName);
            insertArticle.setString(2, articleDesc);
            insertArticle.setString(3, articleImage);
            insertArticle.setDouble(4, articlePrice);
            insertArticle.setInt(5, userId);
            var result = insertArticle.executeUpdate();
            if (result == 0) {
                //error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/articleInsertion"), "Missing dispatcher");
                req.setAttribute("errorQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException("prezzo" + articlePrice, e);
        }
        req.setAttribute("goodInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }
}
