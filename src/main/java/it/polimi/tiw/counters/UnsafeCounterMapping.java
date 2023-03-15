package it.polimi.tiw.counters;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UnsafeCounter
 */
@WebServlet(
        urlPatterns = "/UnsafeCounterMapping",
        initParams = {
                @WebInitParam(name = "servletC", value = "10")
        }
)
public class UnsafeCounterMapping extends HttpServlet {

    private int counter;

    @Override
    public void init() {
        String st1 = getInitParameter("servletC");
        String st2 = getServletContext().getInitParameter("webappC");

        int servletCounter;
        try {
            servletCounter = Integer.parseInt(st1);
        } catch (NumberFormatException e) {
            servletCounter = 0;
        }
        int webappCounter;
        try {
            webappCounter = Integer.parseInt(st2);
        } catch (NumberFormatException e) {
            webappCounter = 0;
        }

        counter = servletCounter + webappCounter;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();

        String resetFlag = req.getParameter("reset");
        if (resetFlag != null && resetFlag.equals("true"))
            counter = 0;  // http://localhost:8080/TIW-servletIntro/UnsafeCounterMapping?reset=true
        else
            counter++; // increment after nonparametric HTTP GET request

        out.println("You have accessed this servlet");
        out.println(counter + " times.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
