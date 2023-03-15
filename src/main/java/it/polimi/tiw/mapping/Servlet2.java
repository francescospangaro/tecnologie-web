package it.polimi.tiw.mapping;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Servlet2
 */
@WebServlet("/baz/*")
public class Servlet2 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Servlet2() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		out.println("<HTML><BODY>");
		out.println("<HEAD><TITLE>Servlet 2 response </TITLE></HEAD>");

		out.println("<P>" + "THIS IS SERVLET2" + "</P>");
		
		out.println("<P> Request URI:   " + request.getRequestURI() + "</P>");

        out.println("<P> Context path: " + request.getContextPath() + "</P>");

		out.println("<P> Servlet path: " + request.getServletPath() + "</P>");

		out.println("<P> Path info: " + request.getPathInfo() + "</P>");

		out.println("<P> Translated path: " + request.getPathTranslated() + "</P>");

		out.println("</HTML></BODY>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
