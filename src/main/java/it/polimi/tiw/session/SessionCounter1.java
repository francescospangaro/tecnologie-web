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
@WebServlet("/SessionCounter1")
public class SessionCounter1 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SessionCounter1() {
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
		out.println("The ID of this session is ");
		out.println(session.getId());
		out.println("You have accessed this servlet ");
		out.println(session.getAttribute("counter") + " times");
		out.println(
				"<p><a href=\"http://localhost:8080/TIW-servletIntro/SessionCounter2\"> Click to access another servlet with the same session </a><p>");
		out.println("</html></body>");
		String resetFlag = req.getParameter("reset");
		if (resetFlag != null && resetFlag.equals("true"))
			session.invalidate();
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
