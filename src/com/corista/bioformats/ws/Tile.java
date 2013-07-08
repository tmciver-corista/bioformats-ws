package com.corista.bioformats.ws;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.io.IOException;

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
@WebServlet(description = "Get a tile image for a given slide data file at given tile offsets.", urlPatterns = { "/Tile" })
public class Tile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String filePath = "/Users/tmciver/Documents/corista-images/Unsupported/Olympus/GI test01.vsi";
	private ImporterOptions options;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tile() {
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		options = new ImporterOptions();
		options.setId(filePath);
		options.setCrop(true);
		
		// set a crop region
		int x = 0;
		int y = 0;
		int width = 256;
		int height = 256;
		options.setCropRegion(0, new Region(x, y, width, height));
		
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
