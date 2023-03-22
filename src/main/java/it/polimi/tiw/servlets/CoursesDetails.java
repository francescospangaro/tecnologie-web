package it.polimi.tiw.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.courses.Course;
import it.polimi.tiw.courses.CoursesManager;

@WebServlet("/getCourseDetails")
public class CoursesDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CoursesDetails() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id_param = request.getParameter("id");

		Integer id = -1;

		boolean bad_request = false;

		if (id_param == null) {
			bad_request = true;
		}

		try {
			id = Integer.parseInt(id_param);
		} catch (NumberFormatException e) {
			bad_request = true;
		}

		if (bad_request) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter id with format number is required");
			return;
		}

		CoursesManager cm = CoursesManager.getCoursesManagerInstance();
		if (!cm.existCourseWithId(id)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Course not present");
			return;
		}

		Course course = cm.getCourseById(id);

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><BODY>");
		out.println("<HEAD><TITLE>Course details</TITLE></HEAD>");
		out.println("<h3>Course details</h3>");
		out.println("<p>Code: " + course.getCode() + "</p>");
		out.println("<p>Name: " + course.getName() + "</p>");
		out.println("<p>Professor: " + course.getProfessor() + "</p>");
		out.println("<p>Hours: " + course.getHours() + "</p>");
		out.println("<p>Description: " + course.getDescription() + "</p>");
		out.println("</HTML></BODY>");
		out.close();
	}

}
