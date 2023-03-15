package it.polimi.tiw.session;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SessionCounter
 */
@WebServlet("/SessionCounter2")
public class SessionCounter2 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SessionCounter2() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		HttpSession session = req.getSession(true); // if session does not exist, create one
		if (session.isNew())
			session.setAttribute("counter", 1);
		else // session already existing
		{
			int toAdd = (int) session.getAttribute("counter") + 1;
			session.setAttribute("counter", toAdd); // update counter for this session
		}
		out.println("<html><body>");
		out.println("<p>The ID of this session is ");
		out.println(session.getId() + "</p>");
		out.println("<p>You have accessed this servlet ");
		out.println(session.getAttribute("counter") + " times</p>");
		out.println("<p>Pathinfo: " + req.getPathInfo() + "</p>");
		out.println("<p>Path " + req.getPathTranslated() + "</p>");
		out.println("</html></body>");

	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
