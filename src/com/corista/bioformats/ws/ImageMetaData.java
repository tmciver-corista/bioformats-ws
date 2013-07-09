package com.corista.bioformats.ws;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.in.ImporterOptions;

/**
 * Servlet implementation class ImageMetaData
 */
@WebServlet(description = "Used to get image meta data such as height and width.", urlPatterns = { "/ImageMetaData" })
public class ImageMetaData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String FILENAME_PARAM_NAME = "filename";
	
	private String imageDir = "/Users/tmciver/Documents/corista-images/Unsupported/Olympus";
	private ImporterOptions options;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageMetaData() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get filename URL param
		String filename = request.getParameter(FILENAME_PARAM_NAME);
		if (filename == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You must supply a filename parameter value.");
			return;
		}
		
		// create OME-XML metadata store
	    ServiceFactory factory = null;
	    OMEXMLService service = null;
	    try {
			factory = new ServiceFactory();
			service = factory.getInstance(OMEXMLService.class);
		} catch (DependencyException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	    
	    IMetadata meta;
		try {
			meta = service.createOMEXMLMetadata();
		} catch (ServiceException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}

	    // create format reader
	    IFormatReader reader = new ImageReader();
	    reader.setMetadataStore(meta);
	    
	    // set the ID (image file name)
	    String imageFile = imageDir + File.separator + filename;
	    try {
			reader.setId(imageFile);
		} catch (FormatException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			return;
		}
	    
	    // get the width and height
	    int width = reader.getSizeX();
	    int height = reader.getSizeY();
	    
	    // format the response as JSONJ
	    String json = String.format("{ \"width\": \"%d\", \"height\": \"%d\" }", width, height);
	    
	    // set up the response
	    response.setContentType("application/json");
	    response.getOutputStream().print(json);
	}
}
