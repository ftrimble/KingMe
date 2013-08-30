package ftrimble.kingme.device.file;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.text.format.Time;
import android.location.Location;
import android.util.Log;

import org.apache.commons.lang.StringUtils;

public class KingMeGPX {

    private static final String XML_VERSION =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String ROOT_NODE =
        "<gpx creator=\"KingMeGPX\" version=\"0.1\" " +
        "xmlns=\"http://www.topografix.com/GPX/1/1\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
        "http://www.topografix.com/GPX/1/1/gpx.xsd " +
        "http://www.garmin.com/xmlschemas/GpxExtensions/v3 " +
        "http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd " +
        "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 " +
        "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd " +
        "http://www.garmin.com/xmlschemas/GpxExtensions/v3 " +
        "http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd " +
        "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 " +
        "http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" " +
        "xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" " +
        "xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\">";

    private final File mFile;
    private final FileWriter mFileWriter;
    private final BufferedWriter mOut;
    private int mIndentLevel;

    public KingMeGPX(File dir, String rideName, Time time) throws IOException {
        mFile = new File(dir, rideName);
        mFileWriter = new FileWriter(mFile);
        mOut = new BufferedWriter(mFileWriter);
        mIndentLevel = 0;

        beginDocument(time, rideName);
    }

    /**
     * Initializes the document data for writing.
     */
    private void beginDocument(Time time, String firstTrackName) {
        try {
            addRootNode();
            addMetaData(time);
            addTrack(firstTrackName);
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not write to GPX file.");
        }
    }

    /**
     * Closes the document when a course is finished.
     */
    public void endDocument() {
        try {
            endSegment();
            endFile();
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not close GPX file.");
        }
    }

    /**
     * Adds the root node and the data accompanying it.
     */
    private void addRootNode() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(XML_VERSION);
        sb.append(addNewLine());
        sb.append(ROOT_NODE);
        ++mIndentLevel;

        mOut.write(sb.toString(),0,sb.toString().length());
    }

    /**
     * Adds whatever metadata we're given.
     */
    private void addMetaData(Time time) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(addNewLine());

        sb.append("<metadata>");
        ++mIndentLevel;

        sb.append(addNewLine());

        sb.append("<time>");
        sb.append(time.format("%FT%TZ"));
        sb.append("</time>");
        --mIndentLevel;

        sb.append(addNewLine());

        sb.append("</metadata>");

        mOut.write(sb.toString(),0,sb.toString().length());
    }

    /**
     * This adds a new track; Context.getApplicationContext() is a full course. In general, we'll only use
     * one for each route on the device.
     */
    private void addTrack(String trackName) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(addNewLine());

        sb.append("<trk>");
        ++mIndentLevel;

        sb.append(addNewLine());

        sb.append("<name>");
        sb.append(trackName);
        sb.append("</name>");

        mOut.write(sb.toString(),0,sb.toString().length());
    }

    /**
     * This adds a new segment. These might indicate laps. Even better, once our
     * segment handling is sufficiently powerful, we can use them to track the
     * users progress on certain segments.
     */
    public void addSegment() {
        StringBuilder sb = new StringBuilder();

        sb.append(addNewLine());

        sb.append("<trkseg>");
        ++mIndentLevel;

        try {
            mOut.write(sb.toString(),0,sb.toString().length());
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not write to GPX file.");
        }
    }

    /**
     * Quick helper function to ensure that there is the correct
     * amount of whitespace for the indentation level.
     */
    private String addNewLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(StringUtils.repeat("  ",mIndentLevel));
        return sb.toString();
    }

    /**
     * Adds data about a specific point in a course.
     */
    public void addPoint(Location location, Time time) {
        if ( location == null ) System.out.println("Received a null location");
        StringBuilder sb = new StringBuilder();

        sb.append(addNewLine());
        sb.append("<trkpt lat=\"");
        sb.append(location.getLatitude());
        sb.append("\" lon=\"");
        sb.append(location.getLongitude());
        sb.append("\">");
        ++mIndentLevel;

        sb.append(addNewLine());


        sb.append("<ele>");
        // TODO calculate location more accurately;
        // perhaps using a web service
        sb.append(location.getAltitude());
        sb.append("</ele>");

        sb.append(addNewLine());

        sb.append("<time>");
        sb.append(time.format("%FT%TZ"));
        sb.append("</time>");
        --mIndentLevel;

        sb.append(addNewLine());

        sb.append("</trkpt>");

        try {
            mOut.write(sb.toString(),0,sb.toString().length());
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not write to GPX file.");
        }
    }

    /**
     * Terminates a segment; the lap / segment is over.
     */
    public void endSegment() {
        StringBuilder sb = new StringBuilder();
        --mIndentLevel;

        sb.append(addNewLine());
        sb.append("</trkseg>");

        try {
            mOut.write(sb.toString(),0,sb.toString().length());
        } catch ( IOException ioe ) {
            Log.d("KingMeGPX","Could not write to GPX file.");
        }
    }


    /**
     * Finishes the course; ends the segment and the root node
     * and closes the file.
     */
    private void endFile() throws IOException {
        StringBuilder sb = new StringBuilder();
        endSegment();
        --mIndentLevel;

        sb.append(addNewLine());
        sb.append("</trk>");
        --mIndentLevel;

        sb.append(addNewLine());
        sb.append("</gpx>");

        mOut.write(sb.toString(),0,sb.toString().length());
        mOut.close();
    }

}