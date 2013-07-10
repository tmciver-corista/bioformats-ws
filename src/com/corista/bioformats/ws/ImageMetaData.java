package com.corista.bioformats.ws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;

/**
 * Servlet implementation class ImageMetaData
 */
@WebServlet(description = "Used to get image meta data such as height and width.", urlPatterns = { "/ImageMetaData" })
public class ImageMetaData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String BASE_DIR_PROPERTY_NAME = "basedir";
	private static final String FILENAME_PARAM_NAME = "filename";

	private String imageDir;
       
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public ImageMetaData() throws IOException {
        
    	// initialize imageDir from a properties file
    	InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("bioformats-ws.properties");
    	Properties props = new Properties();
    	props.load(stream);
    	imageDir = props.getProperty(BASE_DIR_PROPERTY_NAME);
    	if (imageDir == null) {
    		throw new IOException("Could not load property '" + BASE_DIR_PROPERTY_NAME + "'.");
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get filename URL param
		String filename = request.getParameter(FILENAME_PARAM_NAME);
		if (filename == null || filename.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must supply a filename parameter value.");
			return;
		}

	    // create format reader
	    IFormatReader reader = new ImageReader();
	    
	    // set the ID (image file name)
	    String imageFile = imageDir + File.separator + filename;
	    try {
			reader.setId(imageFile);
		} catch (FormatException e) {
			reader.close();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}

	    // get the width and height
	    int width = reader.getSizeX();
	    int height = reader.getSizeY();
	    
	    // close the reader
	    reader.close();
	    
	    // format the response as JSON
	    String json = String.format("{ \"width\": \"%d\", \"height\": \"%d\" }", width, height);
	    
	    // set up the response
	    response.setContentType("application/json");
	    response.getOutputStream().print(json);
	}
}
