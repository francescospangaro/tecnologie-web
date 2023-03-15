package it.polimi.tiw.request;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RequestInfo
 */
@WebServlet("/RequestInfo")
public class RequestInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RequestInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  response.setContentType("text/html");
		  PrintWriter out = response.getWriter();
		  out.println("<HTML>");
		  out.println("<HEAD>");
		  out.println("<TITLE>Request Information Example</TITLE>");
		  out.println("</HEAD>");
		  out.println("<BODY>");
		  out.println("<H3>Request Information Example</H3>");
		  out.println("Method:   " + request.getMethod());
		  out.println("<BR>");
		  out.println("Request URI:   " + request.getRequestURI());
		  out.println("<BR>");
		  out.println("User Agent:"+ request.getHeader("User-Agent"));
		  out.println("</BODY>");
		  out.println("</HTML>");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
