package neandertal.jaugre.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.Edgel;
import neandertal.jaugre.core.data.LineSegment;


/**
 * STEP 3.
 * Using RANSAC for each region to find groups of “inliers”, i.e.
 * edgels which can be fitted into a line. The RANSAC algorithm picks the edgels to 
 * match at random, so this produces a certain degree of uncertainty of the number of
 * detected segments (and everything after that) - this means two subsequent runs on 
 * the same input will produce similar, but not equal results.
 * <br/>
 * Uses:<br/>
 * <a href="http://en.wikipedia.org/wiki/RANSAC">RANSAC algorithm</a> 
 * 
 * @author neandertal
 */
public class SegmentsFinder
{
    /** Maximum iterations to find line segments in a region */
    public static final int DEFAULT_MAX_LINESEGMENT_ITERATIONS = 25;
    /** Maximum iterations to process the edgels in a region */
    public static final int DEFAULT_MAX_EDGELS_ITERATIONS = 100;
    /** Threshold for when the orientation of 2 edgels are compatible */
    public static final float DEFAULT_COMPATIBILITY_VALUE = 0.38f;
    /** Threshold for distance of an edgel to a line to be part of the line */
    public static final float DEFAULT_DISTANCE_TO_LINE = 0.75f;
    /** Minimum inline edgels for an accepted line segment */
    public static final int DEFAULT_MIN_SUPPORT_EDGELS_COUNT = 5;

    /**
     * Detects line segments in the image using default values.
     * @param image contains all data
     * @return map of segments for each region
     */
    public static Map<String, List<LineSegment>> detectSegments(Container image)
    {
        return detectSegments(image, DEFAULT_MIN_SUPPORT_EDGELS_COUNT, DEFAULT_MAX_EDGELS_ITERATIONS,
                DEFAULT_MAX_LINESEGMENT_ITERATIONS, DEFAULT_DISTANCE_TO_LINE, DEFAULT_COMPATIBILITY_VALUE);
    }

    /**
     * Detects line segments in the image using custom values.
     * @param image contains all data
     * @param minEdgelsCount custom minimum inline edgels for an accepted line segment
     * @param maxEdgelsIter custom iterations limit to process edgels in a region
     * @param maxLineIter custom iterations limit to find line segments in regions
     * @param distanceToLine custom distance from a line of Edgel to be accepted as part of the line
     * @param compValue custom threshold for 2 edgels directions to be accepted as part of the same line
     * @return map of segments for each region
     */
    public static Map<String, List<LineSegment>> detectSegments(Container image, int minEdgelsCount, int maxEdgelsIter,
            int maxLineIter, float distanceToLine, float compValue)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getRegionsMap() == null)
        {
            throw new IllegalArgumentException("Regions map can't be NULL!");
        }

        if (image.getEdgelsMap() == null)
        {
            throw new IllegalArgumentException("Edgels map can't be NULL!");
        }

        detectLineSegments(image, minEdgelsCount, maxEdgelsIter, maxLineIter, distanceToLine, compValue);

        return image.getSegmentsMap();
    }

    private static void detectLineSegments(Container image, int minEdgelsCount, int maxEdgelsIter, int maxLineIter,
            float distanceToLine, float compValue)
    {
        Map<String, List<Edgel>> edgelsMap = image.getEdgelsMap();
        Map<String, List<LineSegment>> segmentsMap = new HashMap<String, List<LineSegment>>();

        Iterator<Entry<String, List<Edgel>>> iter = edgelsMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<String, List<Edgel>> entry = iter.next();
            List<LineSegment> lSegmList = detectLineSegmentsInRegion(entry.getValue(), minEdgelsCount, maxEdgelsIter,
                    maxLineIter, distanceToLine, compValue);

            segmentsMap.put(entry.getKey(), lSegmList);
        }

        image.setSegmentsMap(segmentsMap);
    }

    private static List<LineSegment> detectLineSegmentsInRegion(List<Edgel> edgels, int minEdgelsCount,
            int maxEdgelsIter, int maxLineIter, float distanceToLine, float compValue)
    {
        List<Edgel> edgelsCopy = new LinkedList<Edgel>(edgels);
        List<LineSegment> lineSegments = new LinkedList<LineSegment>();
        // end line segments search when there are not enough edgels left for an
        // accepted line segment or iterations limit has been reached
        int iterations = 0;
        while (edgelsCopy.size() >= minEdgelsCount && iterations < maxEdgelsIter)
        {
            iterations++;
            LineSegment strongest = findStrongestLineSegment(edgelsCopy, maxEdgelsIter, maxLineIter, distanceToLine,
                    compValue);

            if (strongest != null && strongest.getInliners().size() >= minEdgelsCount)
            {
                // remove supporting edgels from pool
                edgelsCopy.removeAll(strongest.getInliners());

                // fix linesegment
                fixLineSegment(strongest);

                lineSegments.add(strongest);
            }
        }// while

        return lineSegments;
    }

    // find real edge points of line segment
    // determine direction of segment
    private static void fixLineSegment(LineSegment ls)
    {
        Edgel start = ls.getStart();
        Edgel end = ls.getEnd();

        int dX = Math.abs(start.getX() - end.getX());
        int dY = Math.abs(start.getY() - end.getY());

        if (dX > dY)
        {
            Iterator<Edgel> iter = ls.getInliners().iterator();
            while (iter.hasNext())
            {
                Edgel e = iter.next();
                if (start.getX() > e.getX())
                {
                    start = e;
                }

                if (end.getX() < e.getX())
                {
                    end = e;
                }
            }
        }
        else
        {
            Iterator<Edgel> iter = ls.getInliners().iterator();
            while (iter.hasNext())
            {
                Edgel e = iter.next();
                if (start.getY() > e.getY())
                {
                    start = e;
                }

                if (end.getY() < e.getY())
                {
                    end = e;
                }
            }

        }

        dX = end.getX() - start.getX();
        dY = end.getY() - start.getY();
        float dot = -dX * start.getDirection()[1] + dY * start.getDirection()[0];

        if (dot > 0)
        {
            // invert start-end
            ls.setStart(end);
            ls.setEnd(start);
            dX = -dX;
            dY = -dY;
        }
        else
        {
            ls.setStart(start);
            ls.setEnd(end);
        }

        // calculate line segment direction
        ls.setDirection(getLineDirection(dX, dY));
    }

    // get the normalized direction vector between the 2 points
    protected static float[] getLineDirection(int dX, int dY)
    {
        // calculate line segment direction
        // normalized vector
        float length = (float) Math.sqrt(dX * dX + dY * dY);
        float[] direction = new float[2];
        direction[0] = dX / length;
        direction[1] = dY / length;
        return direction;
    }
    
    // randomly pick 2 points and check if line segment has enough supporters
    // Do this several times and return the line segment with most supporters
    private static LineSegment findStrongestLineSegment(List<Edgel> edgels, int maxEdgelsIter, int maxLineIter,
            float distanceToLine, float compValue)
    {
        LineSegment strongest = null;

        for (int i = 0; i < maxLineIter; i++)
        {
            // Select randomly 2 different edgels with same direction
            Edgel[] randEdgels = new Edgel[2];
            if (randSelectEdgels(edgels, randEdgels, maxEdgelsIter, compValue))
            {
                // Create line segment
                LineSegment lineSegment = new LineSegment(randEdgels[0], randEdgels[1]);

                // Find the supporting edgels of this line segment
                Iterator<Edgel> iter = edgels.iterator();
                while (iter.hasNext())
                {
                    Edgel edg = iter.next();

                    if (isInliner(lineSegment, edg, distanceToLine, compValue))
                    {
                        lineSegment.addInliner(edg);
                    }
                }

                // evaluate line segment
                if (strongest == null)
                {
                    strongest = lineSegment;
                }
                else
                {
                    if (lineSegment.getInliners().size() > strongest.getInliners().size())
                    {
                        strongest = lineSegment;
                    }
                }
            }
        }

        return strongest;
    }

    // Is part of this line segment
    private static boolean isInliner(LineSegment ls, Edgel e, float distanceToLineLimit, float compatibilityValue)
    {
        Edgel a = ls.getInliners().get(0);
        Edgel b = ls.getInliners().get(ls.getInliners().size()-1);

        if (a == e || b == e)
            return true;

        if (!isOrientationCompatible(a.getDirection(), e.getDirection(), compatibilityValue))
        {
            return false;
        }

        return distanceToLine(a, b, e) < distanceToLineLimit;
    }

    // Distance from E to line AB
    private static double distanceToLine(Edgel a, Edgel b, Edgel e)
    {
        int lengABX = b.getX() - a.getX();
        int lengABY = b.getY() - a.getY();
        double normalLength = Math.hypot(lengABX, lengABY);
        return Math.abs((e.getX() - a.getX()) * lengABY - (e.getY() - a.getY()) * lengABX) / normalLength;
    }

    // try to randomly pick 2 edgels from the list with same orientation
    private static boolean randSelectEdgels(List<Edgel> edgels, Edgel[] randEdgels, int maxEdgelsIter,
            float compatibilityValue)
    {
        Random random = new Random();
        int rand1;
        int rand2;
        int iteration = 0;
        while (iteration < maxEdgelsIter)
        {
            iteration++;
            rand1 = random.nextInt(edgels.size());
            rand2 = random.nextInt(edgels.size());

            if (rand1 == rand2)
                continue;

            randEdgels[0] = edgels.get(rand1);
            randEdgels[1] = edgels.get(rand2);

            if (randEdgels[0] == randEdgels[1])
                continue;

            if (isOrientationCompatible(randEdgels[0].getDirection(), randEdgels[1].getDirection(), compatibilityValue))
            {
                return true;
            }
        }

        return false;
    }

    protected static boolean isOrientationCompatible(float[] direction1, float[] direction2, float compatibilityValue)
    {
        return direction1[0] * direction2[0] + direction1[1] * direction2[1] > compatibilityValue;
    }

}
