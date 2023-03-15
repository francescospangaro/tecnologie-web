package it.polimi.tiw.counters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet implementation class UnsafeCounter
 */
public class UnsafeCounter extends HttpServlet {

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
            counter = 0; // http://localhost:8080/TIW-servletIntro/UnsafeCounter?reset=true
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
