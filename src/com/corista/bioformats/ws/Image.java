package com.corista.bioformats.ws;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import loci.common.Region;
import loci.formats.FormatException;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
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
	private static final String MEDIA_TYPE_PARAM_NAME = "type";
	private static final String SERIES_PARAM_NAME = "series";
	
	private static final int DEFAULT_X_COORD = 0;
	private static final int DEFAULT_Y_COORD = 0;
	private static final int DEFAULT_WIDTH = 256;
	private static final int DEFAULT_HEIGHT = 256;
	private static final int DEFAULT_SERIES = 0;
	
	private String imageDir;
	private Map<String, ReaderAndOptions> readerAndOptionsCache = new HashMap<String, ReaderAndOptions>();
       
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
    
    private ReaderAndOptions getReaderAndOptions(String filepath) throws IOException {
    	
    	// see if there's one in the cache
    	ReaderAndOptions readerAndOptions = readerAndOptionsCache.get(filepath);
    	if (readerAndOptions != null) {
    		return readerAndOptions;
    	}
    	
    	// there wasn't one cached; create an ImportOptions object
    	ImporterOptions options = new ImporterOptions();
    	options.setId(filepath);
		options.setCrop(true);
		options.setAutoscale(false);
		
		// create the ImportProcess object
		ImportProcess process = new ImportProcess(options);
		try {
			if (!process.execute()) {
				throw new IOException("Error executing ImportProcess.process().");
			}
		} catch (FormatException e) {
			throw new IOException(e);
		}
		
		// create the ImagePlusReader
		ImagePlusReader reader = new ImagePlusReader(process);
		
		// create the ReaderAndOptions object
		readerAndOptions = new ReaderAndOptions(reader, options);
		
		// add it to the cache
		readerAndOptionsCache.put(filepath, readerAndOptions);
		
		return readerAndOptions;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// get URL params
		// filename
		String filename = request.getParameter(FILENAME_PARAM_NAME);
		if (filename == null || filename.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must supply a filename parameter value.");
			return;
		}
		
		// get the series param
		String seriesStr = request.getParameter(SERIES_PARAM_NAME);
		int series = DEFAULT_SERIES;
		if (seriesStr != null) {
			try {
				series = Integer.parseInt(seriesStr);
			} catch (NumberFormatException nfe) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, SERIES_PARAM_NAME + " parameter was malformed.");
				return;
			}
		}

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
		if (width < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "width parameter cannot be negative.");
			return;
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
		if (height < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "height parameter cannot be negative.");
			return;
		}
		
		// type
		String type = request.getParameter(MEDIA_TYPE_PARAM_NAME);
		if (type == null || type.equals("bmp")) {
			type = "image/bmp";
		} else if (type.equals("jpg")) {
			type = "image/jpeg";
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter value.");
			return;
		}
		
		// get the ImagePlusReader and ImportOptions objects
		String imageFile = imageDir + File.separator + filename;
		ReaderAndOptions readerAndOptions = getReaderAndOptions(imageFile);

		// set a crop region
		readerAndOptions.options.setCropRegion(series, new Region(xCoord, yCoord, width, height));
		
		// set the series
		for (int i = 0; i < series; i++) {
			readerAndOptions.options.setSeriesOn(i, false);
		}
		readerAndOptions.options.setSeriesOn(series, true);
		
		ImagePlus[] imps = null;
		try {
			imps = readerAndOptions.reader.openImagePlus();
		} catch (FormatException fe) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Received a FormatException from BioFormats library.");
			return;
		} catch (IOException ioe) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Received an IOException from BioFormats library.");
			return;
		}
		
		if (imps.length < 1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was no image data received from BioFormats library.");
			return;
		}
		
		// get the image
		BufferedImage image = (BufferedImage)imps[0].getImage();
		
		// write image to output stream
		if (type.equals("image/jpeg")) {
			ImageIO.write(image, "jpg", response.getOutputStream());
			response.setContentType("image/jpeg");
		} else if (type.equals("image/bmp")) {
			ImageIO.write(image, "bmp", response.getOutputStream());
			response.setContentType("image/bmp");
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server tried to process an invalid type argument.");
			return;
		}
	}
}

class ReaderAndOptions {
	
	public ImagePlusReader reader;
	public ImporterOptions options;
	
	public ReaderAndOptions(ImagePlusReader reader, ImporterOptions options) {
		this.reader = reader;
		this.options = options;
	}
}
