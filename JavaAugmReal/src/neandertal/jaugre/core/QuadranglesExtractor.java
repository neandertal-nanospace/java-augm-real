package neandertal.jaugre.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import neandertal.jaugre.core.data.Chain;
import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.LineSegment;
import neandertal.jaugre.core.data.Quadrangle;


/**
 * STEP 8.
 * Extracts quadrangle information from chains by calculating the intersection points of all lines.
 * <br/>
 * Uses:
 * <br/>
 * <a href="http://www.ahristov.com/tutorial/geometry-games/intersection-lines.html">Lines intersection</a>
 * @author neandertal
 */
public class QuadranglesExtractor
{

    /**
     * Extract quadrangle information
     * @param image
     */
    public static Collection<Quadrangle> extractQuadrangles(Container image)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getChains() == null)
        {
            throw new IllegalArgumentException("Chains can't be NULL!");
        }

        extractQuadranglesInternal(image);

        return image.getQuadrangles();
    }

    
    private static void extractQuadranglesInternal(Container image)
    {
        Collection<Quadrangle> quadrangles = new ArrayList<Quadrangle>(); 
        Collection<Chain> chains = image.getChains();
        for(Chain chain: chains)
        {
            LineSegment ls1 = chain.getLines().get(0);
            LineSegment ls2 = chain.getLines().get(1);
            LineSegment ls3 = chain.getLines().get(2);
            LineSegment ls4 = (chain.getLength() > 3) ? chain.getLines().get(3): null;
            
            Point p1 = getConnectingPoint(ls1, ls2);
            Point p2 = getConnectingPoint(ls2, ls3);
            Point p3 = (ls4 != null) ? getConnectingPoint(ls3, ls4): ls3.getEnd().getPoint();
            Point p4 = (ls4 != null) ? getConnectingPoint(ls4, ls1): ls1.getStart().getPoint();
            
            //Some lines are parallel
            if (p1 == null || p2 == null || p3 == null || p4 == null)
            {
                continue;
            }
            
            Quadrangle q = new Quadrangle(p1, p2, p3, p4);
            quadrangles.add(q);
        }
        
        image.setQuadrangles(quadrangles);
    }
    
    /**
     *  Computes the intersection between two lines. The calculated point is approximate, 
     *  since integers are used. If you need a more precise result, use doubles
     *  everywhere. if no intersection, NULL is returned.
     *  (c) 2007 Alexander Hristov. Use Freely (LGPL license). http://www.ahristov.com
     * @param a
     * @param b
     * @return Point intersection
     */
    private static Point getConnectingPoint(LineSegment a, LineSegment b)
    {  
        int x1 = a.getStart().getX();
        int y1 = a.getStart().getY();
        int x2 = a.getEnd().getX();
        int y2 = a.getEnd().getY();
        int x3 = b.getStart().getX();
        int y3 = b.getStart().getY();
        int x4 = b.getEnd().getX();
        int y4 = b.getEnd().getY();
        
        float d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
        if (d == 0) return null;
            
        int xi = (int)(((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d);
        int yi = (int)(((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d);
          
        return new Point(xi,yi);
    }
}
