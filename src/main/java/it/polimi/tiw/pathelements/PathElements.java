package it.polimi.tiw.pathelements;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PathElements
 */
@WebServlet("/PathElements/*")
public class PathElements extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PathElements() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		 
		PrintWriter out = response.getWriter();
		
		out.println("<HTML><BODY>");
		out.println("<HEAD><TITLE>Inspecting the path </TITLE></HEAD>");
		
		out.println("<P>"+request.getContextPath()+"</P>");
		
	    out.println("<P>"+request.getServletPath()+"</P>");
	    
	    out.println("<P>"+request.getPathInfo()+"</P>");
	    
	    out.println("<P>"+request.getPathTranslated()+"</P>");
	    
	    out.println("</HTML></BODY>");
	    
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
