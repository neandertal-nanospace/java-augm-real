package neandertal.jaugre.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import neandertal.jaugre.core.data.Chain;
import neandertal.jaugre.core.data.Edgel;
import neandertal.jaugre.core.data.EdgelTypeEnum;
import neandertal.jaugre.core.data.LineSegment;
import neandertal.jaugre.core.data.Quadrangle;
import neandertal.jaugre.core.data.Region;


/**
 * Helper class, used to debug-paint different parts of the process.
 * 
 * @author neandertal
 */
public class Tools
{
    /**
     * Clone an image
     * 
     * @param img
     * @return
     */
    public static final BufferedImage cloneImage(Image img)
    {
        BufferedImage bi;
        if (img instanceof BufferedImage)
        {
            BufferedImage source = (BufferedImage) img;
            ColorModel cm = source.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = source.copyData(null);
            bi = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        }
        else
        {
            bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(img, 0, 0, null);
        }

        return bi;
    }

    /**
     * Draw segments grid over image
     * 
     * @param bi
     * @param regions
     * @return
     */
    public static final BufferedImage drawRegions(BufferedImage bi, Collection<Region> regions)
    {
        Graphics g = bi.getGraphics();
        g.setColor(new Color(235, 230, 205, 128));
        Iterator<Region> iter = regions.iterator();

        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        while (iter.hasNext())
        {
            Region r = iter.next();
            g.drawLine(r.getX(), r.getY(), r.getX() + r.getWidth() - 1, r.getY());
            g.drawLine(r.getX(), r.getY(), r.getX(), r.getY() + r.getHeight() - 1);

            minX = Math.min(minX, r.getX());
            minY = Math.min(minY, r.getY());
            maxX = Math.max(maxX, r.getX() + r.getWidth());
            maxY = Math.max(maxY, r.getY() + r.getHeight());
        }

        maxX--;
        maxY--;
        g.drawLine(minX, maxY, maxX, maxY);
        g.drawLine(maxX, minY, maxX, maxY);

        return bi;
    }

    /**
     * Draws Edgels over image
     * 
     * @param imgWithSegments
     * @param regions
     */
    public static final BufferedImage drawEdgels(BufferedImage img, Collection<Edgel> edgels)
    {
        Graphics g = img.getGraphics();
        Iterator<Edgel> iter = edgels.iterator();
        while (iter.hasNext())
        {
            Edgel edgel = iter.next();
            g.setColor((edgel.getType() == EdgelTypeEnum.VERTICAl) ? Color.RED : Color.GREEN);
            g.drawLine(edgel.getX(), edgel.getY() - 1, edgel.getX(), edgel.getY() + 1);
            g.drawLine(edgel.getX() - 1, edgel.getY(), edgel.getX() + 1, edgel.getY());
        }

        return img;
    }

    /**
     * Draws Edgels over image
     * 
     * @param imgWithSegments
     * @param regions
     */
    public static final BufferedImage drawLineSegments(BufferedImage img, Collection<LineSegment> lsList)
    {
        Graphics g = img.getGraphics();
        Iterator<LineSegment> iter = lsList.iterator();
        while (iter.hasNext())
        {
            LineSegment ls = iter.next();
            float[] lsDirection = ls.getDirection();
            Edgel start = ls.getStart();
            Edgel end = ls.getEnd();

            g.setColor(Color.RED);
            g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
            g.setColor(Color.GREEN);
            int nX = (int) (end.getX() - 5.0f * (lsDirection[0] - lsDirection[1]));
            int nY = (int) (end.getY() - 5.0f * (lsDirection[0] + lsDirection[1]));
            g.drawLine(end.getX(), end.getY(), nX, nY);

            nX = (int) (end.getX() - 5.0f * (lsDirection[0] + lsDirection[1]));
            nY = (int) (end.getY() + 5.0f * (lsDirection[0] - lsDirection[1]));
            g.drawLine(end.getX(), end.getY(), nX, nY);
        }

        return img;
    }
    
    public static final void printSegmentsCoordinates(String regionName, List<LineSegment> segments)
    {
        if (!segments.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Rectangle=").append(regionName);
            Iterator<LineSegment> iter = segments.iterator();
            while (iter.hasNext())
            {
                LineSegment segm = iter.next();
                sb.append("\n    Segm=[").append(segm.getStart().getX()).append("x").append(segm.getStart().getY());
                sb.append("->").append(segm.getEnd().getX()).append("x").append(segm.getEnd().getY()).append("]");
            }
            System.out.println(sb.toString());
        }
    }
    
    public static final void printEdgelsCoordinates(String regionName, List<Edgel> edgels)
    {
        if (!edgels.isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Rectangle=").append(regionName);
            Iterator<Edgel> iter = edgels.iterator();
            while (iter.hasNext())
            {
                Edgel edg = iter.next();
                sb.append("\n    Edgel=[").append(edg.getX()).append("x").append(edg.getY()).append("]");
            }
            System.out.println(sb.toString());
        }
    }

    public static BufferedImage drawChains(BufferedImage img, Collection<Chain> chains)
    {
        Graphics g = img.getGraphics();
        Iterator<Chain> iter = chains.iterator();
        while (iter.hasNext())
        {
            Chain chain = iter.next();
            Iterator<LineSegment> iter2 = chain.getLines().iterator();
            while (iter2.hasNext())
            {
                LineSegment ls = iter2.next();
                float[] lsDirection = ls.getDirection();
                Edgel start = ls.getStart();
                Edgel end = ls.getEnd();

                g.setColor(Color.BLUE);
                g.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
                g.setColor(Color.BLUE);
                int nX = (int) (end.getX() - 5.0f * (lsDirection[0] - lsDirection[1]));
                int nY = (int) (end.getY() - 5.0f * (lsDirection[0] + lsDirection[1]));
                g.drawLine(end.getX(), end.getY(), nX, nY);

                nX = (int) (end.getX() - 5.0f * (lsDirection[0] + lsDirection[1]));
                nY = (int) (end.getY() + 5.0f * (lsDirection[0] - lsDirection[1]));
                g.drawLine(end.getX(), end.getY(), nX, nY);
            }
        }

        return img;
    }
    
    public static BufferedImage drawQuadrangles(BufferedImage img, Collection<Quadrangle> quadrangles)
    {
        Graphics2D g = (Graphics2D)img.getGraphics();
        for (Quadrangle q: quadrangles)
        {
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.YELLOW);
            g.drawLine(q.getP1().x, q.getP1().y, q.getP2().x, q.getP2().y);
            g.drawLine(q.getP2().x, q.getP2().y, q.getP3().x, q.getP3().y);
            g.drawLine(q.getP3().x, q.getP3().y, q.getP4().x, q.getP4().y);
            g.drawLine(q.getP4().x, q.getP4().y, q.getP1().x, q.getP1().y);
        }

        return img;
    }
}
