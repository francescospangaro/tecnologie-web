package it.polimi.webapp;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;

@WebServlet("/")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try(PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");

            try {
                // Thank you Tomcat for making this still necessary somehow
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if(DriverManager.drivers().findAny().isEmpty()) {
                out.println("Can't find MySQL driver :C");
            } else {
                out.println("Hello this is a test");
            }
        }
    }
}
