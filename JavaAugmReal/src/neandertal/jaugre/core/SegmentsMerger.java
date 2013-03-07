package neandertal.jaugre.core;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.Edgel;
import neandertal.jaugre.core.data.LineSegment;


/**
 * STEP 4.
 * Merges segments into lines, first locally - inside a region, then globally.
 * <br/>
 * Uses:
 * <br/>
 * <a href="http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/">Bresenham line algorithm</a>
 * @author neandertal
 */
public class SegmentsMerger
{
    /** Direction difference threshold */
    public static final float DEFAULT_DIRECTION_DIFFERENCE = 0.1f;//instead of 0.01 - TODO test with more images
    /** Upper limit of distance between 2 segments, candidates for merge, squared */
    public static final int DEFAULT_SQUARE_DISTANCE_LIMIT = 625;// 25*25

    /**
     * Merge segments in image, to produce segments outside regions
     * @param image
     * @return List of merged segments
     */
    public static List<LineSegment> mergeSegments(Container image)
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

        if (image.getSegmentsMap() == null)
        {
            throw new IllegalArgumentException("Segments map can't be NULL!");
        }

        mergeLineSegments(image);

        return image.getMergedSegments();
    }

    private static void mergeLineSegments(Container image)
    {
        List<LineSegment> allMergedSegm = new LinkedList<LineSegment>();

        Iterator<Entry<String, List<LineSegment>>> iter = image.getSegmentsMap().entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<String, List<LineSegment>> entry = iter.next();
            List<LineSegment> regionMergedSegm = mergeSegments(image.getImage(), entry.getValue());

            allMergedSegm.addAll(regionMergedSegm);
        }

        // globally merge segments
        allMergedSegm = mergeSegments(image.getImage(), allMergedSegm);
        image.setMergedSegments(allMergedSegm);
    }

    //Merges compatible segments from the list
    private static List<LineSegment> mergeSegments(BufferedImage img, List<LineSegment> segments)
    {
        List<LineSegment> pool = new LinkedList<LineSegment>(segments);
        List<LineSegment> mergedSegments = new LinkedList<LineSegment>();
        SortedMap<Integer, LineSegment> candidates = new TreeMap<Integer, LineSegment>();

        while (!pool.isEmpty())
        {
            // get a line segment and clone it
            LineSegment clone = pool.remove(0).clone();
            // map for candidates for merger, ordered by their distance from the
            // clone
            candidates.clear();

            // step one - weed out any unsuitable line segments
            Iterator<LineSegment> iter = pool.iterator();
            while (iter.hasNext())
            {
                LineSegment toCheck = iter.next();

                // Check if 2 segments have the same direction
                if (!isSameDirection(clone.getDirection(), toCheck.getDirection()))
                {
                    continue;
                }

                // correctly taken points for line inbetween
                Edgel[] edgels = getOrderedEdgels(clone, toCheck);

                int dX = edgels[2].getX() - edgels[1].getX();
                int dY = edgels[2].getY() - edgels[1].getY();
                float[] direction = SegmentsFinder.getLineDirection(dX, dY);
                // check if the connecting line has the same direction
                if (!isSameDirection(clone.getDirection(), direction))
                {
                    continue;
                }

                // check distance is within limits
                int squaredDistance = getSquaredDistance(edgels[1], edgels[2]);
                if (squaredDistance > DEFAULT_SQUARE_DISTANCE_LIMIT)
                {
                    continue;
                }

                // add in ordered fashion the candidate segment
                candidates.put(squaredDistance, toCheck);
            }

            // step two - process the candidate segments, in ordered fashion, by
            // checking all points from the inbetween line if are edgel points
            boolean hasGrown = false;
            Iterator<LineSegment> iter2 = candidates.values().iterator();
            while (iter2.hasNext())
            {
                LineSegment candidate = iter2.next();

                // correctly take points
                Edgel[] edgels = getOrderedEdgels(clone, candidate);
                
                float[] direction = edgels[1].getDirection();
                // get line points
                int[][] points = getPointsOfLine(edgels[1], edgels[2]);

                float[] normal = new float[] { clone.getDirection()[1], -clone.getDirection()[0]};
                
                boolean toMerge = true;
                for (int i = 0; i < points.length; i++)
                {
                    int x = points[i][0];
                    int y = points[i][1];

                    if (!checkPointIfEdgel(img, x, y, normal, direction))
                    {
                        // one point does not satisfy the conditions, the
                        // between segment is discarded
                        toMerge = false;
                        break;
                    }
                }

                if (!toMerge)
                {
                    continue;
                }

                // merge 2 segments
                clone.setStart(edgels[0]);
                clone.setEnd(edgels[3]);
                pool.remove(candidate);
                hasGrown = true;
            }

            //Check if the clone has merged with other segments
            //if yes - then this is a new segment and must remain in the pool
            //otherwise it can't grow any more and is considered final
            if (hasGrown)
            {
                pool.add(clone);
            }
            else
            {
                mergedSegments.add(clone);
                
                //recalculate line direction
                int dX = clone.getEnd().getX() -  clone.getStart().getX();
                int dY =  clone.getEnd().getY() -  clone.getStart().getY();
                clone.setDirection(SegmentsFinder.getLineDirection(dX, dY));
            }
        }// while

        return mergedSegments;
    }

    //Check if point and nearby points satisfy edgel criteria
    protected static boolean checkPointIfEdgel(BufferedImage img, int x, int y, float[] normal, float[] direction)
    {
        //check if Gauss kernel condition holds
        return (checkPointGauss(img, x, y) &&
                //Check if point direction is within range
                checkPointDirection(img, x, y, direction) &&
                //check point above and below, perpendicular to this point if similar directions
                checkPointDirection(img, (int) (x + normal[0]), (int) (y + normal[1]), direction) &&
                checkPointDirection(img, (int) (x - normal[0]), (int) (y - normal[1]), direction));
    }
    
    // Get the start, startBetween, endBetween and end points
    // in that order from the 2 segments
    private static Edgel[] getOrderedEdgels(LineSegment a, LineSegment b)
    {
        // correctly take points
        Edgel[] edgels = new Edgel[4];
        edgels[0] = a.getStart();//start
        edgels[1] = a.getEnd();  //startBetween
        edgels[2] = b.getStart();//endBetween
        edgels[3] = b.getEnd();  //end
        
        //switch the places if we have to
        int rX1 = Math.min(edgels[0].getX(), edgels[3].getX());
        int rY1 = Math.min(edgels[0].getY(), edgels[3].getY());
        int rX2 = Math.max(edgels[0].getX(), edgels[3].getX());
        int rY2 = Math.max(edgels[0].getY(), edgels[3].getY());
        if (edgels[1].getX() >= rX1 && edgels[1].getX() <= rX2 && edgels[1].getY() >= rY1 && edgels[1].getY() <= rY2)
        {
            return edgels;
        }
        
        edgels[0] = b.getStart();//start
        edgels[1] = b.getEnd();  //startBetween
        edgels[2] = a.getStart();//endBetween
        edgels[3] = a.getEnd();  //end
        return edgels;
    }
    
    // Check if the dot product of the 2 normalized directions is close to 1.0
    private static boolean isSameDirection(float[] a, float[] b)
    {
        return 1 - (a[0] * b[0] + a[1] * b[1]) < DEFAULT_DIRECTION_DIFFERENCE;
    }

    // get distance between the two points
    protected static int getSquaredDistance(Edgel a, Edgel b)
    {
        int x = b.getX() - a.getX();
        int y = b.getY() - a.getY();
        return x * x + y * y;
    }

    // get the list of coordinates of all points on the line between the 2
    // edgels. Excludes the endpoints.
    private static int[][] getPointsOfLine(Edgel s, Edgel e)
    {
        List<int[]> result = new LinkedList<int[]>();

        int x = s.getX();
        int y = s.getY();
        int w = e.getX() - x;
        int h = e.getY() - y;
        int dx1 = 0;
        int dy1 = 0;
        int dx2 = 0;
        int dy2 = 0;

        if (w < 0)
        {
            dx1 = -1;
        }
        else if (w > 0)
        {
            dx1 = 1;
        }

        if (h < 0)
        {
            dy1 = -1;
        }
        else if (h > 0)
        {
            dy1 = 1;
        }

        if (w < 0)
        {
            dx2 = -1;
        }
        else if (w > 0)
        {
            dx2 = 1;
        }

        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (!(longest > shortest))
        {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0)
            {
                dy2 = -1;
            }
            else if (h > 0)
            {
                dy2 = 1;
            }
            dx2 = 0;
        }

        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++)
        {
            result.add(new int[] { x, y });
            numerator += shortest;
            if (!(numerator < longest))
            {
                numerator -= longest;
                x += dx1;
                y += dy1;
            }
            else
            {
                x += dx2;
                y += dy2;
            }
        }

        //hack-remove edge points, they are already processed
        result.remove(0);
        result.remove(result.size() - 1);
        
        int[][] array = new int[][] {};
        return result.toArray(array);
    }

    // Checks if the point is an edge point and therefore can be considered part
    // of the line segment
    protected static boolean checkPointGauss(BufferedImage img, int x, int y)
    {
        // get horizontal neighbor pixels to calculate Gauss kernel value
        int[] gausArea = new int[5];
        gausArea = img.getRGB(x - 2, y, gausArea.length, 1, gausArea, 0, 1);
        int kernelX = EdgelDetector.calculateRedOutputValue(gausArea, 2);

        if (kernelX < EdgelDetector.DEFAULT_THRESHOLD / 2) // TODO why lower the
                                                           // threshold???
        {
            return false;
        }

        // get vertical neighbor pixels to calculate Gauss kernel value
        gausArea = img.getRGB(x, y - 2, 1, gausArea.length, gausArea, 0, 1);
        int kernelY = EdgelDetector.calculateRedOutputValue(gausArea, 2);

        if (kernelY < EdgelDetector.DEFAULT_THRESHOLD / 2) // TODO why lower the
                                                           // threshold???
        {
            return false;
        }

        return true;
    }

    // Calculate the point direction and compare to the original direction
    protected static boolean checkPointDirection(BufferedImage img, int x, int y, float[] direction)
    {
        // get neighbor pixels and calculate direction of candidate point
        int[] area = img.getRGB(x - 1, y - 1, 3, 3, null, 0, 3);
        float[] pointDirection = EdgelDetector.calculateDirection(area);

        // check if direction compatible
        return SegmentsFinder.isOrientationCompatible(pointDirection, direction,
                SegmentsFinder.DEFAULT_COMPATIBILITY_VALUE);
    }
}