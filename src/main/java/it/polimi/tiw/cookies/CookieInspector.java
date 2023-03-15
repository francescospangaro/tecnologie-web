package it.polimi.tiw.cookies;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * Servlet implementation class CookieInspector
 */
@WebServlet("/CookieInspector")
public class CookieInspector extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // print out cookies
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            out.println("No cookies this time");
        else
            for (int i = 0; i < cookies.length; i++) {
                Cookie c = cookies[i];
                String name = c.getName();
                String value = c.getValue();
                out.println(name + " = " + value);
            }
        // set cookie to user provided value
        String name = request.getParameter("name");
        if (name != null && name.length() > 0) {
            String value = request.getParameter("value");
            Cookie c = new Cookie(name, value);
            c.setMaxAge(3600);
            response.addCookie(c);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
