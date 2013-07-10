package com.corista.bioformats.ws;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import loci.common.Region;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

/**
 * Servlet implementation class Tile
 */
@WebServlet(description = "Get a tile image for a given slide data file at given tile offsets.", urlPatterns = { "/Image" })
public class Image extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String BASE_DIR_PROPERTY_NAME = "basedir";
	private static final String FILENAME_PARAM_NAME = "filename";
	private static final String X_COORD_PARAM_NAME = "xCoord";
	private static final String Y_COORD_PARAM_NAME = "yCoord";
	private static final String WIDTH_PARAM_NAME = "width";
	private static final String HEIGHT_PARAM_NAME = "height";
	
	private static final int DEFAULT_X_COORD = 0;
	private static final int DEFAULT_Y_COORD = 0;
	private static final int DEFAULT_WIDTH = 256;
	private static final int DEFAULT_HEIGHT = 256;
	
	private String imageDir;
	private ImporterOptions options;
       
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public Image() throws IOException {
        
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
		options = new ImporterOptions();
		options.setCrop(true);
		
		// get URL params
		// filename
		String filename = request.getParameter(FILENAME_PARAM_NAME);
		if (filename == null || filename.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must supply a filename parameter value.");
			return;
		}
		
		// set the options ID (image file name)
		String imageFile = imageDir + File.separator + filename;
		options.setId(imageFile);
		
		// x coordinate
		int xCoord = DEFAULT_X_COORD;
		String xStr = request.getParameter(X_COORD_PARAM_NAME);
		if (xStr != null) {
			try {
				xCoord = Integer.parseInt(xStr);
			} catch (NumberFormatException nfe) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "xCoord parameter was malformed.");
				return;
			}
		}
		if (xCoord < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "xCoord parameter cannot be negative.");
			return;
		}
		
		// y coordinate
		int yCoord = DEFAULT_Y_COORD;
		String yStr = request.getParameter(Y_COORD_PARAM_NAME);
		if (yStr != null) {
			try {
				yCoord = Integer.parseInt(yStr);
			} catch (NumberFormatException nfe) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "yCoord parameter was malformed.");
				return;
			}
		}
		if (yCoord < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "yCoord parameter cannot be negative.");
			return;
		}
		
		// width
		int width = DEFAULT_WIDTH;
		String widthStr = request.getParameter(WIDTH_PARAM_NAME);
		if (widthStr != null) {
			try {
				width = Integer.parseInt(widthStr);
			} catch (NumberFormatException nfe) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Width parameter was malformed.");
				return;
			}
		}
		
		// height
		int height = DEFAULT_HEIGHT;
		String heightStr = request.getParameter(HEIGHT_PARAM_NAME);
		if (heightStr != null) {
			try {
				height = Integer.parseInt(heightStr);
			} catch (NumberFormatException nfe) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Height parameter was malformed.");
				return;
			}
		}
		
		// set a crop region
		options.setCropRegion(0, new Region(xCoord, yCoord, width, height));
		
		ImagePlus[] imps = null;
		try {
			imps = BF.openImagePlus(options);
		} catch (FormatException fe) {
			System.err.println("Caught FormatException");
			throw new IOException(fe);
		} catch (IOException ioe) {
			System.err.println("Caught IOException");
			throw ioe;
		}
		
		if (imps.length < 1) {
			throw new IOException("Could not read image data.");
		}
		
		// get the image
		BufferedImage image = (BufferedImage)imps[0].getImage();
		
		// write jpg to output stream
		ImageIO.write(image, "jpg", response.getOutputStream());
		
		// set content type
		response.setContentType("image/jpeg");
	}

}
