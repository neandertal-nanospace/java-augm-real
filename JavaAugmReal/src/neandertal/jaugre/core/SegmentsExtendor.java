package neandertal.jaugre.core;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.Edgel;
import neandertal.jaugre.core.data.LineSegment;


/** 
 * STEP 5.
 * Extends line segments along edges
 * 
 * @author neandertal
 * 
 */
public class SegmentsExtendor
{
    /**
     * Extend segments along edges in image
     * 
     * @param image
     * @return List of extended segments
     */
    public static List<LineSegment> extendSegments(Container image)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getMergedSegments() == null)
        {
            throw new IllegalArgumentException("Merged segments can't be NULL!");
        }

        extendLineSegments(image);

        return image.getExtendedSegments();
    }

    // Extend each end until possible. If line falls outside image - drop it.
    private static void extendLineSegments(Container image)
    {
        List<LineSegment> extended = new LinkedList<LineSegment>();
        int xLowerLimit = image.getLeftInset();
        int yLowerLimit = image.getTopInset();
        int xUpperLimit = image.getImage().getWidth() - image.getRightInset();
        int yUpperLimit = image.getImage().getHeight() - image.getBottomInset();

        Iterator<LineSegment> iter = image.getMergedSegments().iterator();
        while (iter.hasNext())
        {
            // clone line segment
            LineSegment clone = iter.next().clone();

            // extend first end
            extendLineSegment(image.getImage(), clone, true, xLowerLimit, yLowerLimit, xUpperLimit, yUpperLimit);
            // extend last end
            extendLineSegment(image.getImage(), clone, false, xLowerLimit, yLowerLimit, xUpperLimit, yUpperLimit);

            extended.add(clone);
        }

        image.setExtendedSegments(extended);
    }

    protected static void extendLineSegment(BufferedImage image, LineSegment segment, boolean extendStart,
            int xLowerLimit, int yLowerLimit, int xUpperLimit, int yUpperLimit)
    {
        float[] grow = new float[] { segment.getDirection()[0], segment.getDirection()[1] };
        float[] normal = new float[] { segment.getDirection()[1], -segment.getDirection()[0] };
        float x = segment.getEnd().getX();
        float y = segment.getEnd().getY();
        float[] direction = segment.getEnd().getDirection();

        if (extendStart)
        {
            grow[0] = -grow[0];
            grow[1] = -grow[1];
            x = segment.getStart().getX();
            y = segment.getStart().getY();
            direction = segment.getStart().getDirection();
            normal[0] = -normal[0];
            normal[1] = -normal[1];
        }

        int xLast = 0;
        int yLast = 0;
        while (true)
        {
            xLast = (int) x;
            yLast = (int) y;
            x += grow[0];
            y += grow[1];

            // goes outside image, stop
            if (x < xLowerLimit || x >= xUpperLimit || y < yLowerLimit || y >= yUpperLimit)
            {
                break;
            }

            if (!SegmentsMerger.checkPointIfEdgel(image, (int) x, (int) y, normal, direction))
            {
                // end of line reached
                break;
            }
        }// while

        Edgel newEnd = new Edgel();
        newEnd.setDirection(direction);
        newEnd.setX(xLast);
        newEnd.setY(yLast);

        // new end
        if (extendStart)
        {
            newEnd.setType(segment.getStart().getType());
            segment.setStart(newEnd);
        }
        else
        {
            newEnd.setType(segment.getEnd().getType());
            segment.setEnd(newEnd);
        }
    }
}
