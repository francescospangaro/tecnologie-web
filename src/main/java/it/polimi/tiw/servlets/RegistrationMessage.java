package it.polimi.tiw.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/registrationmessage")
public class RegistrationMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public RegistrationMessage() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String message = request.getParameter("message");

		if (message == null || message.isEmpty()) {
			message = "Your registration was succesfully saved";
		}
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<HTML><BODY>");
		out.println("<HEAD><TITLE>Registration</TITLE></HEAD>");
		out.println("<h3>Registration</h3>");
		out.println("<p>" + message + "</p>");

		out.println("</HTML></BODY>");
		out.close();
	}

}
