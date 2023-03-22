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

@WebServlet("/courses")
public class CourseList extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CourseList() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><BODY>");
		out.println("<HEAD><TITLE>Show file</TITLE></HEAD>");
		out.println("<h3>List of courses</h3>");
		out.println("<p>Click on the link of a course for details:</p>");

		CoursesManager cm = CoursesManager.getCoursesManagerInstance();

		for (Course course : cm.getCourses()) {
			out.println("<a href=getCourseDetails?id=" + course.getId() + ">" + course.getName() + "</a><br>");
		}
		out.println(
				"<br><br><br><h4>Fail cases</h4><p>For test purposes invoke the servlet with invalid or missing parameters</p>");
		out.println("<a href=getCourseDetails?id=99> Course id does not exist </a>(/getCourseDetails?id=99)<br>");
		out.println(
				"<a href=getCourseDetails?id=2a> Course id is invalid, it is not a number </a> (/getCourseDetails?id=2a)<br>");
		out.println("<a href=getCourseDetails> Course id parameter not sent </a> (/getCourseDetails)<br>");
		out.println("</HTML></BODY>");
		out.close();
	}

}
