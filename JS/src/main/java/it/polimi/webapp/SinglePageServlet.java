package it.polimi.webapp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class SinglePageServlet extends HttpServlet {

    private String pagePath;

    @Override
    @Initializer
    public void init() {
        this.pagePath = Objects.requireNonNull(
                getServletConfig().getInitParameter("pagePath"),
                "Missing pagePath in SinglePageServlet");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher view = req.getRequestDispatcher(pagePath);
        view.forward(req, resp);
    }
}
