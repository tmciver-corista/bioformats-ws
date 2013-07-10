# bioformats-ws

A web service frontend to the BioFormats library.

# API

Currently there are two resources in bioformats-ws: Image and ImageMetaData.

## Image

The Image resource responds to HTTP GET only.  There are currently five URL
parameters that can be given one of which is required (the rest having default
values).  The required parameter, 'filename', gives the name of the image file
from which a (sub)image will be retrieved.  This image file is with respect to
the path given by the property 'basedir' which should be found in the file
bioformats-ws.properties.  For a deployment on Tomcat, this file can be found in
bioformats-ws/WEB-INF/classes directory.  Be sure to set this to a value
suitable for the system being deployed to.

The four other URL parameters describe the geometry of the desired sub image.
They are 'xCoord', 'yCoord', 'width' and 'height'.  The first two give the
coordinates, in pixels, of the location of the top left corner of the desired
sub image.  The last two are self explanatory.  The default values of these
parameters are 0, 0, 256, 256, respectively.

## ImageMetaData

The ImageMetaData resource responds to HTTP GET only.  There is one required URL
parameter: 'filename' (same as for the Image resource).  Currently this resource
returns only the width and height of the given image.  It is formatted as JSON.
