package it.polimi.tiw.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.courses.Course;
import it.polimi.tiw.courses.CoursesManager;

@WebServlet("/createCourse")
public class CreateCourse extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CreateCourse() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String code = request.getParameter("code");
		String name = request.getParameter("name");
		String professor = request.getParameter("professor");
		String description = request.getParameter("description");
		String hours_param = request.getParameter("hours");

		if (code == null || code.isEmpty() || name == null || name.isEmpty() || professor == null || professor.isEmpty()
				|| description == null || description.isEmpty() || hours_param == null || hours_param.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}

		Integer hours = -1;
		try {
			hours = Integer.parseInt(hours_param);
			// We assume in this example that a course with more than 50 cannot be created
			if (hours > 50 || hours < 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The hours must be bewteween 0 and 50");
				return;
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}

		CoursesManager cm = CoursesManager.getCoursesManagerInstance();
		// The ID is not part of the input provided by the user, it is something that the programmer decides how to assign when creating a new entity
		// We will learn how this is done with a database in following lessons
		Integer id = cm.getNextId();

		Course course = new Course(id, code, name, professor, description, hours);

		cm.insertCourse(course);

		response.sendRedirect("courses");
	}

}
