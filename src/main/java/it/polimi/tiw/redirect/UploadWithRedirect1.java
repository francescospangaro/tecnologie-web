package it.polimi.tiw.redirect;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class UploadNoredirect
 */
@WebServlet("/UploadWithRedirect1")
public class UploadWithRedirect1 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadWithRedirect1() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(true); // if session does not exist, create one
		if (session.isNew())
			session.setAttribute("amount", Integer.parseInt(request.getParameter("cc")));
		else // session already existing
		{
			Integer toAdd = (Integer) session.getAttribute("amount");
			if (toAdd == null) {
				toAdd = 0;
			}
			Integer newAmount = Integer.parseInt(request.getParameter("cc"));
			session.setAttribute("amount", toAdd + newAmount); // update total
		}
		response.sendRedirect("/webapp/UploadWithRedirect2");

	}

}
