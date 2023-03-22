package it.polimi.tiw.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Servlet implementation class UploadImage
 */
@WebServlet("/UploadImage")
@MultipartConfig
public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;

	String folderPath = "";

	public void init() throws ServletException {
		folderPath = getServletContext().getInitParameter("outputpath");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">

		// We first check the parameter needed is present
		if (filePart == null || filePart.getSize() <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
			return;
		}

		// We then check the parameter is valid (in this case right format)
		String contentType = filePart.getContentType();
		System.out.println("Type " + contentType);

		if (!contentType.startsWith("image")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File format not permitted");
			return;
		}

		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		System.out.println("Filename: " + fileName);

		// save outside deploy path
		/*
		 * SAVE OUTSIDE DEPLOY PATH TO AVOID PROBLEMS!
		 * https://stackoverflow.com/a/18664715 YOU HAVE TO CREATE A FOLDER AND PUT THE
		 * ABSOLUTE FOLDER PATH HERE!
		 */
		// String folderPath = "/Users/federico/Documents/Files/";

		String outputPath = folderPath + fileName; //folderPath inizialized in init
		System.out.println("Output path: " + outputPath);

		File file = new File(outputPath);

		try (InputStream fileContent = filePart.getInputStream()) {
			// TODO: WHAT HAPPENS IF A FILE WITH THE SAME NAME ALREADY EXISTS?
			// you could override it, send an error or 
			// rename it, for example, if I need to upload images to an album,
			// and for each image I also save other data,
			// I could save the image as {image_id}.jpg using the id of the db

			Files.copy(fileContent, file.toPath());
			System.out.println("File saved correctly!");

			response.sendRedirect("ShowImage?filename=" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving file");
		}

	}

}
