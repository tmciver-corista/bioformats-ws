package com.corista.bioformats.ws;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Main
 */
@WebServlet("/")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String BASE_DIR_PROPERTY_NAME = "basedir";
	
	//private String imageDir;
	private File imageDir;
       
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public Main() throws IOException {
        
    	// initialize imageDir from a properties file
    	InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("bioformats-ws.properties");
    	Properties props = new Properties();
    	props.load(stream);
    	String imageDirPath = props.getProperty(BASE_DIR_PROPERTY_NAME);
    	if (imageDirPath == null) {
    		throw new IOException("Could not load property '" + BASE_DIR_PROPERTY_NAME + "'.");
    	}
    	imageDir = new File(imageDirPath);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get all files in imageDir
		File[] files = imageDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return !file.isDirectory();
			}
		});
		
		// create html
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>");
		sb.append("BioFormats Web Service - Image List");
		sb.append("</title></head><body><ul>");
		
		for (File file : files) {
			sb.append("<li><a href=\"/bioformats-ws/Image?filename=");
			sb.append(file.getName());
			sb.append("\">");
			sb.append(file.getName());
			sb.append("</a> (<a href=\"/bioformats-ws/ImageMetaData?filename=");
			sb.append(file.getName());
			sb.append("\">Meta</a>)</li>");
		}
		
		sb.append("</ul></body></html>");
		
		response.getOutputStream().print(sb.toString());
		response.setContentType("text/html");
	}

}
