package neandertal.jaugre.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.LineSegment;


/**
 * STEP 6.
 * Detect corners of segment lines.
 * Keep lines with at least one corner.
 * Take a pixel of a line edge a couple of pixels further.
 * If this pixel is ‘white’, we might have found a corner of a black on white marker.
 * @author neandertal
 */
public class CornerDetector
{
    /** thresholds upper and lower for each color channel, inclusive */
    public static int[][] DEFAULT_ENDGE_CHECK_THRESHOLDS = new int[][] {{128, 256},//red lower and upper thresholds
                                                                        {128, 256},//green lower and upper thresholds
                                                                        {128, 256}};//blue lower and upper thresholds
    
    
    /**
     * Weed out lines without a corner end.
     * 
     * @param image
     * @return List of extended segments
     */
    public static List<LineSegment> detectCorners(Container image)
    {
        return detectCorners(image, DEFAULT_ENDGE_CHECK_THRESHOLDS);
    }
    
    /**
     * Weed out lines without a corner end.
     * 
     * @param image
     * @return List of extended segments
     */
    public static List<LineSegment> detectCorners(Container image, int[][] thresholds)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getExtendedSegments() == null)
        {
            throw new IllegalArgumentException("Merged segments can't be NULL!");
        }

        detectCornersInternal(image, thresholds);

        return image.getCornerSegments();
    }

    //Find lines which end at a corner
    private static void detectCornersInternal(Container image, int[][] thresholds)
    {
        List<LineSegment> linesWithCorners = new LinkedList<LineSegment>();
        Iterator<LineSegment> iter = image.getExtendedSegments().iterator();
        while (iter.hasNext())
        {
            LineSegment segment = iter.next();
            float dX = segment.getDirection()[0] * 4f;
            float dY = segment.getDirection()[1] * 4f;
            
            //check startpoint (4 pixels further)
            float x = segment.getStart().getX() - dX;
            float y = segment.getStart().getY() - dY;
            segment.setStartCorner(checkPoint(image, (int) x, (int) y, thresholds));
            
            //check endpoint  (4 pixels further)
            x = segment.getEnd().getX() + dX;
            y = segment.getEnd().getY() + dY;
            segment.setEndCorner(checkPoint(image, (int) x, (int) y, thresholds));
            
            if (segment.isStartCorner() || segment.isEndCorner())
            {
                linesWithCorners.add(segment);
            }
        }
        
        image.setCornerSegments(linesWithCorners);
    }

    // check if point is within threshold limits for each channel
    private static boolean checkPoint(Container image, int x, int y, int[][] thresholds)
    {
        int xLimit = image.getImage().getWidth() - image.getRightInset();
        int yLimit = image.getImage().getHeight() - image.getBottomInset();
        //goes outside image, return
        if (x < image.getLeftInset() || x >= xLimit || y < image.getTopInset() || y >= yLimit)
        {
            return false;
        }
        
        //check red channel
        int pixel = image.getImage().getRGB(x, y);
        int red = (pixel >> 16) & 0xFF;
        if (red < thresholds[0][0] || red > thresholds[0][1])
        {
            return false;//outside threshold
        }
        
        int green = (pixel >> 8) & 0xFF;
        if (green < thresholds[1][0] || green > thresholds[1][1])
        {
            return false;//outside threshold
        }
        
        int blue = pixel & 0xFF;
        if (blue < thresholds[2][0] || blue > thresholds[2][1])
        {
            return false;//outside threshold
        }
        
        return true;
    }
}
